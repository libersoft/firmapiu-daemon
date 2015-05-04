/**
 * 
 */
package it.libersoft.firmapiud.dbusinterface;

import it.libersoft.firmapiu.Data;
import it.libersoft.firmapiu.ResultInterface;
//import it.libersoft.firmapiu.DataFilePath;
//import it.libersoft.firmapiu.GenericArgument;
//import it.libersoft.firmapiu.MasterFactoryBuilder;
//import it.libersoft.firmapiu.cades.CadesBESCommandInterface;
import it.libersoft.firmapiu.cades.CadesBESFactory;
import it.libersoft.firmapiu.cades.P7FileCommandInterface;
import it.libersoft.firmapiu.exception.FirmapiuException;
import it.libersoft.firmapiu.cades.CommandProxyInterface;
import it.libersoft.firmapiu.crtoken.PKCS11Token;
import it.libersoft.firmapiu.crtoken.TokenFactoryBuilder;
import it.libersoft.firmapiu.data.DataFactoryBuilder;
import it.libersoft.firmapiu.data.DataFile;
import static it.libersoft.firmapiu.consts.FactoryConsts.*;
import static it.libersoft.firmapiu.consts.FactoryPropConsts.*;
import static it.libersoft.firmapiu.consts.ArgumentConsts.*;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.types.DBusStructType;

/**
 * @author dellanna
 *
 */
public final class FirmapiuDImpl implements FirmapiuDInterface {

	//FIXME da cambiare nel momento in cui si riscrive libreria
	private final CommandProxyInterface cmdInterface;
	//interfaccia di comandi specializzata per la gestione di file .p7m .p7s
	//private final P7FileCommandInterface p7CommandInterface;
	//resource bundle di FirmapiuD
	private final ResourceBundle localrb;
	
	//overide del path dove si carica la libreria per leggere la carta
	private final static String DPATH="/etc/firmapiu";
	
	private int prova;
	
	public FirmapiuDImpl() {
		super();
		ResourceBundle rb = ResourceBundle.getBundle("it.libersoft.firmapiud.lang.locale",Locale.getDefault());
		this.localrb = ResourceBundle.getBundle("it.libersoft.firmapiud.lang.locale",Locale.getDefault());
		//FIXME da cambiare nel momento in cui si riscrive libreria
		this.cmdInterface=new CommandProxyInterface(rb);
		//this.p7CommandInterface = CadesBESFactory.getFactory().getP7FileCommandInterface();
		this.prova=0;
	}

	/* (non-Javadoc)
	 * @see org.freedesktop.dbus.DBusInterface#isRemote()
	 */
	@Override
	public boolean isRemote() {
		return false;
	}

	/* (non-Javadoc)
	 * @see it.libersoft.firmapiud.dbusinterface.FirmapiuDInterface#sign(java.lang.String[], java.util.Map)
	 */
	@Override
	public Map<String, Variant<?>> sign(Variant<?>[] args,
			Map<String, Variant<?>> options) {
		System.out.println("Stesso oggetto?? prova ->"+prova);
		prova++;
		
		//fa l'unmarshalling dei parametri di ingresso e del pin
		DataFile dataFile= DataFactoryBuilder.getFactory(DATAFILEFACTORY).getDataFile();
		String pin = unmarshallDataFile(dataFile, args, options);
		
		if(pin==null)
			throw new DBusExecutionException(localrb.getString("error0p")+" : "+TOKENPIN);
		
		//overide del path dove si carica la libreria per leggere la carta
		//commandoptions.put(CommandProxyInterface.DRIVERPATH, DPATH);
		
		//in caso di eccezione la rilancia come errore di dbus
		//Crea l'interfaccia di comando e si logga sul token
		ResultInterface<File,File> result=null;
		//TODO da rivedere per ottimizzare tempo/memoria
		PKCS11Token pkcs11Token = null;
		try {
			//crea il token per fare la firma 
			pkcs11Token = TokenFactoryBuilder.getFactory(PKCS11TOKENFACTORY).getPKCS11Token(CRTSMARTCARD);
			P7FileCommandInterface p7CommandInterface = CadesBESFactory.getFactory().getP7FileCommandInterface(pkcs11Token,null);
			//si logga sul token, firma e si slogga
			pkcs11Token.login(pin.toCharArray());
			result=p7CommandInterface.sign(dataFile);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBusExecutionException(localrb.getString("error3f")+" : "+e.getLocalizedMessage());
		}finally{
			try {
				pkcs11Token.logout();
			} catch (FirmapiuException e) {
				e.printStackTrace();
				throw new DBusExecutionException(localrb.getString("error3f"));
			} catch (Exception e){}
		}
		
		//fa il marshalling del risultato ottenuto
		return marshallFile(result);
		
//			if(oldValue instanceof String)
//			{
//				newValue=new Variant<Object>(oldValue,"s");
//			}
//			else if(oldValue instanceof FirmapiuException)
//			{
//				//String str=oldValue.getClass().getCanonicalName()+" : "+((Exception)oldValue).getLocalizedMessage();
//				FirmapiuException fe1=(FirmapiuException) oldValue;
//				FirmapiuExceptionStruct struct = new FirmapiuExceptionStruct(fe1.errorCode,fe1.getLocalizedMessage());
//				newValue=new Variant<Object>(struct,"(is)");
//			}else
//				throw new DBusExecutionException(localrb.getString("error3f")+" : "+localrb.getString("error4f"));
//			dbusResult.put(key, newValue);
//		}//fine while
		
//		return dbusResult;
	}

	/* (non-Javadoc)
	 * @see it.libersoft.firmapiud.dbusinterface.FirmapiuDInterface#verify(java.lang.String[])
	 */
	@Override
	public Map<String,String> verify(Variant<?>[] args) {
		//FIXME da cambiare quando si cambia libreria
		//prepara i parametri da passare a firmapiulib
		if (args==null || args.length==0)
			throw new DBusExecutionException(localrb.getString("error0"));
		Set<String> commandargs=new TreeSet<String>();
		for(Variant<?> arg : args)
			commandargs.add((String)arg.getValue());
		
		Map<String,?> result=this.cmdInterface.verify(commandargs, null);
		
		//effettua il marshalling dei risultati da inviare a dbus
		Map<String,String> dbusResult = new TreeMap<String,String>();
		Iterator<String> itr=result.keySet().iterator();
		while(itr.hasNext()){
			String key=itr.next();
			Object oldValue=result.get(key);
			String newValue;
			if(oldValue instanceof Boolean)
			{
				newValue=new String(localrb.getString("verify0")+" : "+(Boolean)oldValue);
			}
			else if(oldValue instanceof Exception)
			{
				//FIXME da fissare con struct
				String str="666 : "+oldValue.getClass().getCanonicalName()+" : "+((Exception)oldValue).getLocalizedMessage();
				newValue=str;
			}else
				throw new DBusExecutionException(localrb.getString("error3v")+" : "+localrb.getString("error4v"));
			dbusResult.put(key, newValue);
		}//fine while
		return dbusResult;
	}
	
	@Override
	public Map<String, Variant<?>> getContentSignedData(Variant<?>[] args,
			Map<String, Variant<?>> options) {
		//fa l'unmarshalling dei parametri di ingresso
		DataFile dataFile= DataFactoryBuilder.getFactory(DATAFILEFACTORY).getDataFile();
		unmarshallDataFile(dataFile, args, options);
		
		//crea l'interfaccia di comando e recupera il contenuto originale dei file
		ResultInterface<File,File> result=null;
		
		P7FileCommandInterface p7CommandInterface=CadesBESFactory.getFactory().getP7FileCommandInterface(null,null);
		try {
			result=p7CommandInterface.getContentSignedData(dataFile);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBusExecutionException(localrb.getString("error3c")+" : "+e.getLocalizedMessage());
		}
		//fa il marshalling del risultato ottenuto
		return marshallFile(result);
	}
	
	//PROCEDURE PRIVATE
	//TODO ricorda di definire nella documentazione le opzioni messe a disposizione e i tipi che dbus associa a queste opzioni
	
	//effettua l'unmarshalling delle opzioni ricevute in maniera tale da convertire il formato del valore delle opzioni
	//ricevute da dbus in quello da passare alla libreria
//	private Object unmarshall(String key,Variant<?> value){
//		if(key.equals(CommandProxyInterface.PIN)){
//			//si aspetta che il valore passato abbia signature = "s" e lo trasforma in char[]
//			if(value.getSig().equals("s")){
//				return ((String)(value.getValue())).toCharArray();
//			}
//			else throw new DBusExecutionException(key+" : "+localrb.getString("error1"));
//		}else if(key.equals(CommandProxyInterface.OUTDIR)){
//			//si aspetta che il valore passato abbia signature = "s", non effettua trasformazioni
//			if(value.getSig().equals("s")){
//				return ((String)(value.getValue()));
//			}
//			else throw new DBusExecutionException(key+" : "+localrb.getString("error1"));
//		}
//		else
//			//se non c'è nessuna opzione valida lancia un errore
//			throw new DBusExecutionException(key+" : "+localrb.getString("error1"));
//	}
	
	//fa l'unmashalling dei parametri riveuti in ingresso
	//restituisce il pin (può essere uguale a null)
	private String unmarshallDataFile(DataFile dataFile, Variant<?>[] args,Map<String, Variant<?>> options ){
		//FIXME da cambiare nel momento in cui si riscrive libreria
		//linka l'implementazione concreta del demone alla libreria firmapiulib
		//prepara i parametri da passare a firmapiulib
		if (args==null || args.length==0)
			throw new DBusExecutionException(localrb.getString("error0"));
		//DataFile dataFile= DataFactoryBuilder.getFactory(DATAFILEFACTORY).getDataFile();
		for(Variant<?> arg : args){
			try {
				File file = new File((String)arg.getValue());
				dataFile.setData(file);
			} catch (FirmapiuException e) {
				e.printStackTrace();
				throw new DBusExecutionException(localrb.getString("error0")+" : <"+e.errorCode+"> "+e.getLocalizedMessage());
			}
		}
		//setta le opzioni
		//GenericArgument arguments=null;
		String pin=null;
		if(options!=null){
			//arguments=(GenericArgument)MasterFactoryBuilder.getFactory(ARGUMENTFACTORY).getArgument(GENERICARGUMENT);
			Iterator<String> itr=options.keySet().iterator();
			while(itr.hasNext()){
				//deve fare l'unmarshalling delle opzioni da quelle ricevute in ingresso a quelle richieste da sign
				String key=itr.next();
				//il pin deve essere salvato a parte
				String value = unmarshallOptions(options.get(key));
				if (key.equals(TOKENPIN))
					pin=value;
				else
					try {
						dataFile.setArgument(key, value);
					} catch (FirmapiuException e) {
						e.printStackTrace();
						throw new DBusExecutionException(localrb.getString("error3f")+" : "+e.getLocalizedMessage());
					}
			}
		}
		
		return pin;
	}//fine metodo
	
	private String unmarshallOptions(Variant<?> value){
		//i tipi dei valori possono essere solo basic types (Stringhe e Boolean)
		if(value.getSig().equals("s")){
			return (String)(value.getValue());
		}else if (value.getSig().equals("b")){
			Boolean valBool = (Boolean)(value.getValue());
			return valBool.toString();
		}else
			throw new DBusExecutionException(localrb.getString("error0"));	
	}

	//fa il marshalling dei risultati ottenuti in uscita
	private Map<String,Variant<?>> marshallFile(ResultInterface<File, File>result){
		//effettua il marshalling dei risultati da inviare a dbus
		Map<String,Variant<?>> dbusResult = new TreeMap<String,Variant<?>>();
		Iterator<File> itr=null;
		try {
			itr = result.getResultDataSet().iterator();
		} catch (FirmapiuException e) {
			e.printStackTrace();
			throw new DBusExecutionException(localrb.getString("error3f")+" : "+e.getLocalizedMessage());
		}
		while(itr.hasNext()){
			File keyFile=itr.next();
			String key=keyFile.getAbsolutePath();
			Variant<?> newValue;
			try {
				File oldValue=result.getResult(keyFile);
				newValue=new Variant<String>(oldValue.getAbsolutePath(),"s");
			} catch (FirmapiuException e) {
				e.printStackTrace();
				FirmapiuExceptionStruct struct = new FirmapiuExceptionStruct(e.errorCode,e.getLocalizedMessage());
				newValue=new Variant<FirmapiuExceptionStruct>(struct,FirmapiuExceptionStruct.class);
			}
			dbusResult.put(key, newValue);
		}//fine while

		return dbusResult;
	}
}
