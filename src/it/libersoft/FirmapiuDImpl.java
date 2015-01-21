/**
 * 
 */
package it.libersoft;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusExecutionException;

import firmapiu.CommandProxyInterface;

/**
 * @author andy
 *
 */
public final class FirmapiuDImpl implements FirmapiuDInterface {

	//FIXME da cambiare nel momento in cui si riscrive libreria
	private final CommandProxyInterface cmdInterface;
	//resource bundle di FirmapiuD
	private final ResourceBundle localrb;
	
	public FirmapiuDImpl() {
		super();
		ResourceBundle rb = ResourceBundle.getBundle("firmapiu.lang.locale",Locale.getDefault());
		this.localrb = ResourceBundle.getBundle("firmapiud.lang.locale",Locale.getDefault());
		//FIXME da cambiare nel momento in cui si riscrive libreria
		this.cmdInterface=new CommandProxyInterface(rb);
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
	public Map<String, Variant<?>> sign(String[] args,
			Map<String, Variant<?>> options) {
		// TODO Auto-generated method stub
		//FIXME da cambiare nel momento in cui si riscrive libreria
		//linka l'implementazione concreta del demone alla libreria firmapiulib
		//prepara i parametri da passare a firmapiulib
		if (args==null || args.length==0)
			throw new DBusExecutionException(localrb.getString("error0"));
		Set<String> commandargs=new TreeSet<String>();
		for(String arg : args)
			commandargs.add(arg);
		
		Map<String,Object> commandoptions=null;
		if(options!=null){
			commandoptions=new TreeMap<String,Object>();
			Iterator<String> itr=options.keySet().iterator();
			while(itr.hasNext()){
				//deve fare l'unmarshalling delle opzioni da quelle ricevute in ingresso a quelle richieste da sign
				String key=itr.next();
				Object value = unmarshall(key, options.get(key));
				commandoptions.put(key, value);
			}
		}
		
		//in caso di eccezione la rilancia come errore di dbus
		Map<String,?> result;
		try {
			result=cmdInterface.sign(commandargs, commandoptions);
		} catch (Exception e) {
			// TODO Auto-generated catch block: da sistemare i log
			e.printStackTrace();
			throw new DBusExecutionException(localrb.getString("error3")+" : "+e.getLocalizedMessage());
		}
		
		//effettua il marshalling dei risultati da inviare a dbus
		Map<String,Variant<?>> dbusResult = new TreeMap<String,Variant<?>>();
		Iterator<String> itr=result.keySet().iterator();
		while(itr.hasNext()){
			String key=itr.next();
			Object oldValue=result.get(key);
			Variant<?> newValue;
			if(oldValue instanceof String)
			{
				newValue=new Variant<Object>(oldValue,"s");
			}
			else if(oldValue instanceof Exception)
			{
				//FIXME da fissare con struct
				String str="666 : "+oldValue.getClass().getCanonicalName()+" : "+((Exception)oldValue).getLocalizedMessage();
				newValue=new Variant<Object>(str,"s");
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
	public Map<String,String> verify(String[] args) {
		// TODO Auto-generated method stub
		//FIXME da cambiare quando si cambia libreria
		//prepara i parametri da passare a firmapiulib
		if (args==null || args.length==0)
			throw new DBusExecutionException(localrb.getString("error0"));
		Set<String> commandargs=new TreeSet<String>();
		for(String arg : args)
			commandargs.add(arg);
		
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
