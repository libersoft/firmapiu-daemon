/**
 * 
 */
package it.libersoft.firmapiud.dbusinterface;

import it.libersoft.firmapiu.Data;
import it.libersoft.firmapiu.Report;
import it.libersoft.firmapiu.ResultInterface;
//import it.libersoft.firmapiu.DataFilePath;
//import it.libersoft.firmapiu.GenericArgument;
//import it.libersoft.firmapiu.MasterFactoryBuilder;
//import it.libersoft.firmapiu.cades.CadesBESCommandInterface;
import it.libersoft.firmapiu.cades.CadesBESFactory;
import it.libersoft.firmapiu.cades.P7FileCommandInterface;
import it.libersoft.firmapiu.exception.FirmapiuException;
import it.libersoft.firmapiu.cades.CommandProxyInterface;
import it.libersoft.firmapiu.crtoken.KeyStoreToken;
import it.libersoft.firmapiu.crtoken.PKCS11Token;
import it.libersoft.firmapiu.crtoken.TokenFactoryBuilder;
import it.libersoft.firmapiu.data.DataFactoryBuilder;
import it.libersoft.firmapiu.data.DataFile;
import static it.libersoft.firmapiu.consts.FactoryConsts.*;
import static it.libersoft.firmapiu.consts.FactoryPropConsts.*;
import static it.libersoft.firmapiu.consts.ArgumentConsts.*;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bouncycastle.cms.SignerInformation;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.types.DBusStructType;

/**
 * 
 * @author dellanna
 *
 */
public final class FirmapiuDImpl implements FirmapiuDInterface {
	//TODO ricorda di definire nella documentazione le opzioni messe a disposizione e i tipi che dbus associa a queste opzioni
	
	//FIXME da cambiare nel momento in cui si riscrive libreria
	private final CommandProxyInterface cmdInterface;
	//interfaccia di comandi specializzata per la gestione di file .p7m .p7s
	//private final P7FileCommandInterface p7CommandInterface;
	//resource bundle di FirmapiuD
	private final ResourceBundle localrb;
	
	//overide del path dove si carica la libreria per leggere la carta
	private final static String DPATH="/etc/firmapiu";
	
	private int prova;
	
	//keystore Token contente le CA usate come trust anchor
	private KeyStoreToken tslKeystoreToken;
	
	public FirmapiuDImpl() {
		super();
		ResourceBundle rb = ResourceBundle.getBundle("it.libersoft.firmapiud.lang.locale",Locale.getDefault());
		this.localrb = ResourceBundle.getBundle("it.libersoft.firmapiud.lang.locale",Locale.getDefault());
		//FIXME da cambiare nel momento in cui si riscrive libreria
		this.cmdInterface=new CommandProxyInterface(rb);
		//this.p7CommandInterface = CadesBESFactory.getFactory().getP7FileCommandInterface();
		this.prova=0;
		//keystore token di una lista di CA fidate definite in una Trusted Store List.
		try {
			this.tslKeystoreToken=TokenFactoryBuilder.getFactory(KEYSTORETOKENFACTORY).getKeyStoreToken(TSLXMLKEYSTORE);
		} catch (FirmapiuException e) {
			e.printStackTrace();
		}
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
//		//FIXME da cambiare quando si cambia libreria
//		//prepara i parametri da passare a firmapiulib
//		if (args==null || args.length==0)
//			throw new DBusExecutionException(localrb.getString("error0"));
//		Set<String> commandargs=new TreeSet<String>();
//		for(Variant<?> arg : args)
//			commandargs.add((String)arg.getValue());
//		
//		Map<String,?> result=this.cmdInterface.verify(commandargs, null);
//		
//		//effettua il marshalling dei risultati da inviare a dbus
//		Map<String,String> dbusResult = new TreeMap<String,String>();
//		Iterator<String> itr=result.keySet().iterator();
//		while(itr.hasNext()){
//			String key=itr.next();
//			Object oldValue=result.get(key);
//			String newValue;
//			if(oldValue instanceof Boolean)
//			{
//				newValue=new String(localrb.getString("verify0")+" : "+(Boolean)oldValue);
//			}
//			else if(oldValue instanceof Exception)
//			{
//				//FIXME da fissare con struct
//				String str="666 : "+oldValue.getClass().getCanonicalName()+" : "+((Exception)oldValue).getLocalizedMessage();
//				newValue=str;
//			}else
//				throw new DBusExecutionException(localrb.getString("error3v")+" : "+localrb.getString("error4v"));
//			dbusResult.put(key, newValue);
//		}//fine while
//		return dbusResult;
		// TODO funzionalità di verifica di piu di un file non supportata: E' possibile che la funzionalità venga supportata in futuro
		throw new DBusExecutionException(localrb.getString("error5"));
	}
	
	@Override
	public Map<String, Variant<?>>[] verifySingle(Variant<?> arg,
			Map<String, Variant<?>> options) {
		//TODO implementare un controllo meno rigido sul token usato per la verifica
		//recupera il token contenente il keystore delle "trust anchor" utilizzate per controllare affidabilità del firmatario
		if(this.tslKeystoreToken==null)
			throw new DBusExecutionException(localrb.getString("error3vt"));
		
		//fa l'unmarshalling dei parametri di ingresso
		DataFile dataFile= DataFactoryBuilder.getFactory(DATAFILEFACTORY).getDataFile();
		unmarshallOptions(dataFile, options);
		//se è definita l'opzione detached ed è true deve verificare un p7s
		boolean detached=false;
		if(options!=null && options.containsKey(DETACHED)){
			Variant<?> val=options.get(DETACHED);
			if (val.getSig().equals("b")){
				detached=(Boolean)val.getValue();
			}
		}
		if(detached){
			//verifica un p7s: la variant passata come argomento deve essere ss
			//TODO da supportare
			throw new DBusExecutionException(localrb.getString("error5"));
		}//fine ramo p7s
		else{
			//verifica un p7m: la variant passata come argomento deve essere s
			//unmashall file da firmare
			try {
				if (arg.getSig().equals("s")){
					File file = new File((String)arg.getValue());
					dataFile.setData(file);
				} else
					throw new DBusExecutionException(localrb.getString("error0v")+" : <"+arg.getSig()+">");
			} catch (FirmapiuException e) {
				e.printStackTrace();
				throw new DBusExecutionException(localrb.getString("error0")+" : <"+e.errorCode+"> "+e.getLocalizedMessage());
			}
		
			//crea l'interfaccia di comando e verifica la busta crittografica passata come parametro
			ResultInterface<File,Report> result=null;
			
			P7FileCommandInterface p7CommandInterface=CadesBESFactory.getFactory().getP7FileCommandInterface(null,this.tslKeystoreToken);
			try {
				result=p7CommandInterface.verify(dataFile);
			} catch (FirmapiuException e) {
				e.printStackTrace();
				throw new DBusExecutionException(localrb.getString("error3v")+" : "+e.getLocalizedMessage());
			}
			
			return marshallVerifyResult(result);
		}//fine ramo p7m
	}//fine metodo
	
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
	private String unmarshallDataFile(DataFile dataFile, Variant<?>[] args,Map<String, Variant<?>> options){
		if (args==null || args.length==0)
			throw new DBusExecutionException(localrb.getString("error0"));
		for(Variant<?> arg : args){
			try {
				if (arg.getSig().equals("s")){
					File file = new File((String)arg.getValue());
					dataFile.setData(file);
				} else
					throw new DBusExecutionException(localrb.getString("error0v")+" : <"+arg.getSig()+">");
			} catch (FirmapiuException e) {
				e.printStackTrace();
				throw new DBusExecutionException(localrb.getString("error0")+" : <"+e.errorCode+"> "+e.getLocalizedMessage());
			}
		}
		
		return unmarshallOptions(dataFile, options);
	}//fine metodo
	
	//fa l'unmarshalling delle opzioni associate al comando
	private String unmarshallOptions(DataFile dataFile,Map<String, Variant<?>> options){
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
				String value = unmarshallOptionsProcedure(options.get(key));
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
	}
	
	//procedura privata eseguita durante l'unmarshalling delle opzioni
	private String unmarshallOptionsProcedure(Variant<?> value){
		//i tipi dei valori possono essere solo basic types (Stringhe e Boolean)
		if(value.getSig().equals("s")){
			return (String)(value.getValue());
		}else if (value.getSig().equals("b")){
			Boolean valBool = (Boolean)(value.getValue());
			return valBool.toString();
		}else
			throw new DBusExecutionException(localrb.getString("error0"));	
	}

	//fa il mashalling del risultato ottenuto dall'operazione di verifica
	public Map<String, Variant<?>>[] marshallVerifyResult(ResultInterface<File, Report> result){
		//recupera il report associato all'unico risultato che dovrebbe essere stato ottenuto
		Set<File> resultSet;
		try {
			resultSet = result.getResultDataSet();
		} catch (FirmapiuException e3) {
			e3.printStackTrace();
			throw new DBusExecutionException(localrb.getString("error4v")+" : "+e3.getLocalizedMessage());
		}
		if(resultSet.size()!=1)
			throw new IllegalArgumentException("Abbiamo un problema huston");
		File keyFile=resultSet.iterator().next();
		Report verifyReport;
		try {
			verifyReport = result.getResult(keyFile);
		} catch (FirmapiuException e3) {
			e3.printStackTrace();
			throw new DBusExecutionException(localrb.getString("error3v")+" : "+e3.getLocalizedMessage());
		}
		//recupera la lista dei firmatari e crea la struttura dati in risposta da inviare a Dbus
		List<SignerInformation> signerList;
		try {
			signerList = verifyReport.getSigners();
		} catch (FirmapiuException e2) {
			e2.printStackTrace();
			throw new DBusExecutionException(localrb.getString("error4v")+" : "+e2.getLocalizedMessage());
		}
		ArrayList<TreeMap<String, Variant<?>>> dbusResultList = new ArrayList<TreeMap<String, Variant<?>>>();
		Iterator<SignerInformation> signerListItr = signerList.iterator();
		while(signerListItr.hasNext()){
			SignerInformation signer=signerListItr.next();
			Set<String> signerRecordFields;
			try {
				signerRecordFields = verifyReport.getSignerRecordFields(signer);
			} catch (FirmapiuException e1) {
				e1.printStackTrace();
				throw new DBusExecutionException(localrb.getString("error4v")+" : "+e1.getLocalizedMessage());
			}
			TreeMap<String, Variant<?>> dbusRecord= new TreeMap<String, Variant<?>>();
			Iterator<String> fieldItr= signerRecordFields.iterator();
			while(fieldItr.hasNext()){
				String field=fieldItr.next();
				try {
					Object fieldValue = verifyReport.getSignerField(signer, field);
					dbusRecord.put(field,obj2Variant(fieldValue));
				} catch (FirmapiuException e) {
					// in caso di eccezione aggiunge l'errore come Variant (is)
					e.printStackTrace();
					FirmapiuExceptionStruct struct = new FirmapiuExceptionStruct(e.errorCode,e.getLocalizedMessage());
					Variant<?> newValue=new Variant<FirmapiuExceptionStruct>(struct,FirmapiuExceptionStruct.class);
					dbusRecord.put(field,obj2Variant(newValue));
				}
			}
			dbusResultList.add(dbusRecord);
		}
		return (Map<String, Variant<?>>[])dbusResultList.toArray();
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
			throw new DBusExecutionException(localrb.getString("error3m")+" : "+e.getLocalizedMessage());
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
	
	//trasforma un oggetto in una variant
	private static Variant<?> obj2Variant(Object obj){
		//per il momento gestisce solo boolean e string
		if(obj instanceof String)
			return new Variant<String>((String)obj,"s");
		if(obj instanceof Boolean)
			return new Variant<Boolean>((Boolean)obj,"b");
		throw new IllegalArgumentException("Cannot tranform Object to Variant!");
	}
}
