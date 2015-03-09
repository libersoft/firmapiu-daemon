/**
 * 
 */
package it.libersoft;

import it.libersoft.firmapiu.Data;
import it.libersoft.firmapiu.DataFilePath;
import it.libersoft.firmapiu.GenericArgument;
import it.libersoft.firmapiu.MasterFactoryBuilder;
import it.libersoft.firmapiu.cades.CadesBESCommandInterface;
import it.libersoft.firmapiu.exception.FirmapiuException;
import it.libersoft.firmapiu.cades.CommandProxyInterface;
import static it.libersoft.firmapiu.consts.FactoryConsts.*;
import static it.libersoft.firmapiu.consts.FactoryPropConsts.*;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusExecutionException;

/**
 * @author andy
 *
 */
public final class FirmapiuDImpl implements FirmapiuDInterface {

	//FIXME da cambiare nel momento in cui si riscrive libreria
	private final CommandProxyInterface cmdInterface;
	private final CadesBESCommandInterface cadesBesInterface;
	//resource bundle di FirmapiuD
	private final ResourceBundle localrb;
	
	//overide del path dove si carica la libreria per leggere la carta
	private final static String DPATH="/etc/firmapiu";
	
	private int prova;
	
	public FirmapiuDImpl() {
		super();
		ResourceBundle rb = ResourceBundle.getBundle("it.libersoft.firmapiu.lang.locale",Locale.getDefault());
		this.localrb = ResourceBundle.getBundle("firmapiud.lang.locale",Locale.getDefault());
		//FIXME da cambiare nel momento in cui si riscrive libreria
		this.cmdInterface=new CommandProxyInterface(rb);
		cadesBesInterface = MasterFactoryBuilder.getFactory(CADESBESFACTORY).getCadesBESCommandInterface(P7MFILE);
		this.prova=0;
	}

	/* (non-Javadoc)
	 * @see org.freedesktop.dbus.DBusInterface#isRemote()
	 */
	@Override
	public boolean isRemote() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see it.libersoft.FirmapiuDInterface#sign(java.lang.String[], java.util.Map)
	 */
	@Override
	public Map<String, Variant<?>> sign(Variant<?>[] args,
			Map<String, Variant<?>> options) {
		System.out.println("Stesso oggetto?? prova ->"+prova);
		prova++;
		
		// TODO Auto-generated method stub
		//FIXME da cambiare nel momento in cui si riscrive libreria
		//linka l'implementazione concreta del demone alla libreria firmapiulib
		//prepara i parametri da passare a firmapiulib
		if (args==null || args.length==0)
			throw new DBusExecutionException(localrb.getString("error0"));
		DataFilePath dataFile= (DataFilePath)MasterFactoryBuilder.getFactory(DATAFACTORY).getData(DATAFILEPATH);
		for(Variant<?> arg : args){
			try {
				dataFile.setData((String)arg.getValue());
			} catch (FirmapiuException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new DBusExecutionException(localrb.getString("error0")+" : <"+e.errorCode+"> "+e.getLocalizedMessage());
			}
		}
		
		GenericArgument arguments=null;
		if(options!=null){
			arguments=(GenericArgument)MasterFactoryBuilder.getFactory(ARGUMENTFACTORY).getArgument(GENERICARGUMENT);
			Iterator<String> itr=options.keySet().iterator();
			while(itr.hasNext()){
				//deve fare l'unmarshalling delle opzioni da quelle ricevute in ingresso a quelle richieste da sign
				String key=itr.next();
				Object value = unmarshall(key, options.get(key));
				arguments.setArgument(key, value);
			}
		}
		
		//overide del path dove si carica la libreria per leggere la carta
		//commandoptions.put(CommandProxyInterface.DRIVERPATH, DPATH);
		
		//in caso di eccezione la rilancia come errore di dbus
		Map<?,?> result;
		try {
			result=cadesBesInterface.sign(dataFile, arguments);
		} catch (Exception e) {
			// TODO Auto-generated catch block: da sistemare i log
			e.printStackTrace();
			throw new DBusExecutionException(localrb.getString("error3f")+" : "+e.getLocalizedMessage());
		}
		
		//effettua il marshalling dei risultati da inviare a dbus
		Map<String,Variant<?>> dbusResult = new TreeMap<String,Variant<?>>();
		Iterator<?> itr=result.keySet().iterator();
		while(itr.hasNext()){
			String key=(String)itr.next();
			Object oldValue=result.get(key);
			Variant<?> newValue;
			if(oldValue instanceof String)
			{
				newValue=new Variant<Object>(oldValue,"s");
			}
			else if(oldValue instanceof FirmapiuException)
			{
				//FIXME da fissare con struct
				//String str=oldValue.getClass().getCanonicalName()+" : "+((Exception)oldValue).getLocalizedMessage();
				FirmapiuException fe1=(FirmapiuException) oldValue;
				FirmapiuExceptionStruct struct = new FirmapiuExceptionStruct(fe1.errorCode,fe1.getLocalizedMessage());
				newValue=new Variant<Object>(struct,"(is)");
			}else
				throw new DBusExecutionException(localrb.getString("error3f")+" : "+localrb.getString("error4f"));
			dbusResult.put(key, newValue);
		}//fine while
		
		return dbusResult;
	}

	/* (non-Javadoc)
	 * @see it.libersoft.FirmapiuDInterface#verify(java.lang.String[])
	 */
	@Override
	public Map<String,String> verify(Variant<?>[] args) {
		// TODO Auto-generated method stub
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
	
	
	//PROCEDURE PRIVATE
	//TODO ricorda di definire nella documentazione le opzioni messe a disposizione e i tipi che dbus associa a queste opzioni
	
	//effettua l'unmarshalling delle opzioni ricevute in maniera tale da convertire il formato del valore delle opzioni
	//ricevute da dbus in quello da passare alla libreria
	private Object unmarshall(String key,Variant<?> value){
		if(key.equals(CommandProxyInterface.PIN)){
			//si aspetta che il valore passato abbia signature = "s" e lo trasforma in char[]
			if(value.getSig().equals("s")){
				return ((String)(value.getValue())).toCharArray();
			}
			else throw new DBusExecutionException(key+" : "+localrb.getString("error1"));
		}else if(key.equals(CommandProxyInterface.OUTDIR)){
			//si aspetta che il valore passato abbia signature = "s", non effettua trasformazioni
			if(value.getSig().equals("s")){
				return ((String)(value.getValue()));
			}
			else throw new DBusExecutionException(key+" : "+localrb.getString("error1"));
		}
		else
			//se non c'è nessuna opzione valida lancia un errore
			throw new DBusExecutionException(key+" : "+localrb.getString("error1"));
	}
}
