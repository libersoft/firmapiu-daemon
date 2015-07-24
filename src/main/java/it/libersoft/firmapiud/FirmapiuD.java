/**
 * 
 */
package it.libersoft.firmapiud;

import it.libersoft.firmapiud.dbusinterface.FirmapiuDImpl;
import it.libersoft.firmapiud.dbusinterface.FirmapiuDInterface;
import it.libersoft.firmapiud.dbusinterface.TokenManagerImpl;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DirectConnection;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * @author dellanna
 *
 */
public class FirmapiuD {

	//carica oggetto per gestire le operazioni su una busta crittografica
	final static String BUSNAME1="it.libersoft.firmapiud.dbusinterface.FirmapiuDInterface";
	final static String OBJECTPATH1="/it/libersoft/firmapiud/FirmapiuD";
	
	//carica oggetto per la gestione di un token crittografico
	final static String BUSNAME2="it.libersoft.firmapiud.dbusinterface.TokenManagerInterface";
	final static String OBJECTPATH2="/it/libersoft/firmapiud/TokenManager";
	
	//carica il resouce bundle
	private	final static ResourceBundle rb = ResourceBundle.getBundle("it.libersoft.firmapiud.lang.locale",Locale.getDefault());
	//carica il logger//Logger associato alla classe
	private final static Logger LOGGER=Logger.getLogger(FirmapiuD.class.getCanonicalName());
	
	/*final static String BUSNAME="org.freedesktop.FirmapiuDInterface";
	final static String OBJECTPATH="/org/freedesktop/FirmapiuD";*/
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {	
		//setta esplicitamente il path della libreria nativa libunix-java.so poiché 
		//dbus potrebbe non essere in grado di trovarlo
		//System.setProperty("java.library.path", "/usr/lib/jni");
		System.out.println(System.getProperty("java.library.path"));
		/*System.load("/usr/lib/jni/libunix-java.so");
		System.loadLibrary("unix-java");*/
		
		//se args è nullo usa il il bus di sistema di dbus, altrimenti cerca di creare una connessione diretta con il nome
		//dell'argomento passato come parametro
		
		//Si connette al demone dbus
		DBusConnection dbusconn=null;
		DirectConnection directConn = null;
		try {
			LOGGER.info(rb.getString("connect0"));
			if(args==null || args.length==0)
				dbusconn = DBusConnection.getConnection(DBusConnection.SESSION); 
			else
				directConn = new DirectConnection("unix:abstract="+args[0]+",listen=true"); 
			LOGGER.info(rb.getString("connect1"));
		} catch (DBusException e) {
			LOGGER.severe(rb.getString("connect3"));
			// TODO Auto-generated catch block
			e.printStackTrace();
			//non è riuscito a connettersi il programma esce
			System.exit(-1);
		}//fine try-catch
		
		//una volta che la DBusConnection è stata creata aggiunge una procedura per il rilascio delle risorse
		//in caso di shutdown forzato
		if(args==null || args.length==0)
			Runtime.getRuntime().addShutdownHook(
				new FirmapiuDFinalizer(dbusconn,rb));
		else
			Runtime.getRuntime().addShutdownHook(
					new DirectConnectionFinalizer(directConn,rb));
		if (args==null || args.length==0) {
			//acquisisce il dbusname (l'interfaccia) del gestore dei comandi della busta crittografica
			//esporta l'oggetto che gestisce la busta crittografica
			startDBusObjectProcedure(dbusconn, BUSNAME1, FirmapiuDImpl.class, OBJECTPATH1);
			//acquisisce il dbusname (l'interfaccia) del gestore del token crittografico
			//esporta l'oggetto che gestisce il token crittografico
			startDBusObjectProcedure(dbusconn, BUSNAME2, TokenManagerImpl.class, OBJECTPATH2);
		}else{
			//carica direttamente gli oggetti senza caricare il dbusname
			startDirectObjectProcedure(directConn, FirmapiuDImpl.class, OBJECTPATH1);
			startDirectObjectProcedure(directConn, TokenManagerImpl.class, OBJECTPATH2);
		}
		
	}//fine main

	//procedura per inizializzare un oggetto su una connessione diretta
	private static void startDirectObjectProcedure(DirectConnection directConn,Class<?> objClass,String objectPath){
		//cerca di inizializzare l'oggetto
		//FirmapiuDInterface firmapiud= new FirmapiuDImpl();
		DBusInterface dbusInterface=null;
		try {
			Object obj = objClass.newInstance();
			if(obj instanceof DBusInterface)
				dbusInterface = (DBusInterface)obj;
			else 
				throw new IllegalArgumentException("Code Error! You shouldn't see this exception!");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		try {
			LOGGER.info(rb.getString("export0")+": "+objectPath);
			directConn.exportObject(objectPath, dbusInterface);
			LOGGER.info(rb.getString("export1"));
		} catch (DBusException e) {
			LOGGER.severe(rb.getString("export3")+": "+objectPath);
			directConn.disconnect();
			LOGGER.info(rb.getString("close3"));
			System.exit(-1);
		}//fine try-catch
	}//fine metodo
	
	//procedura per inizializzare un oggetto su DBUS
	private static void startDBusObjectProcedure(DBusConnection dbusconn,String busName,Class<?> objClass,String objectPath){
		//cerca di inizializzare l'oggetto
		//FirmapiuDInterface firmapiud= new FirmapiuDImpl();
		DBusInterface dbusInterface=null;
		try {
			Object obj = objClass.newInstance();
			if(obj instanceof DBusInterface)
				dbusInterface = (DBusInterface)obj;
			else 
				throw new IllegalArgumentException("Code Error! You shouldn't see this exception!");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		//inizializza il busname
		try {
			LOGGER.info(rb.getString("bus0")+": "+busName);
			dbusconn.requestBusName(busName);
			LOGGER.info(rb.getString("bus1"));
		} catch (DBusException e) {
			LOGGER.severe(rb.getString("bus3"));
			e.printStackTrace();
			//il programma non è riuscito ad acquisire il bus name richiesto. Cerca di disconnettersi e poi esce
			dbusconn.disconnect();
			LOGGER.info(rb.getString("close3"));
			System.exit(-1);
		}//fine try-catch
		
		try {
			LOGGER.info(rb.getString("export0")+": "+objectPath);
			dbusconn.exportObject(objectPath, dbusInterface);
			LOGGER.info(rb.getString("export1"));
		} catch (DBusException e) {
			LOGGER.severe(rb.getString("export3")+": "+objectPath);
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Il programma non è riuscito ad esportare l'oggetto: rilascia il busname si disconnette ed esce
			try {
				dbusconn.releaseBusName(busName);
				LOGGER.info(rb.getString("close1")+": "+busName);
			} catch (DBusException e1) {
				LOGGER.severe(rb.getString("close2")+": "+busName);
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			dbusconn.disconnect();
			LOGGER.info(rb.getString("close3"));
			System.exit(-1);
		}//fine try-catch		
	}//fine metodo
}
