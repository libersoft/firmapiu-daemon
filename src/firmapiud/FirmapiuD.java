/**
 * 
 */
package firmapiud;

import it.libersoft.FirmapiuDImpl;
import it.libersoft.FirmapiuDInterface;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * @author andy
 *
 */
public class FirmapiuD {

	final static String BUSNAME="it.libersoft.FirmapiuDInterface";
	final static String OBJECTPATH="/it/libersoft/FirmapiuD";
	/*final static String BUSNAME="org.freedesktop.FirmapiuDInterface";
	final static String OBJECTPATH="/org/freedesktop/FirmapiuD";*/
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//carica il resouce bundle
		ResourceBundle rb = ResourceBundle.getBundle("firmapiud.lang.locale",Locale.getDefault());
		//carica il logger//Logger associato alla classe
		final Logger LOGGER=Logger.getLogger(FirmapiuD.class.getCanonicalName());
		
		//Si connette al demone dbus
		DBusConnection dbusconn=null;
		try {
			LOGGER.info(rb.getString("connect0"));
			dbusconn = DBusConnection.getConnection(DBusConnection.SESSION); 
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
		Runtime.getRuntime().addShutdownHook(
				new FirmapiuDFinalizer(dbusconn,rb));
		
		//acquisisce il dbusname (l'interfaccia)
		try {
			LOGGER.info(rb.getString("bus0")+": "+BUSNAME);
			dbusconn.requestBusName(BUSNAME);
			LOGGER.info(rb.getString("bus1"));
		} catch (DBusException e) {
			LOGGER.severe(rb.getString("bus3"));
			// TODO Auto-generated catch block
			e.printStackTrace();
			//il programma non è riuscito ad acquisire il bus name richiesto. Cerca di disconnettersi e poi esce
			dbusconn.disconnect();
			LOGGER.info(rb.getString("close3"));
			System.exit(-1);
		}//fine try-catch
		
		//esporta l'oggetto
		FirmapiuDInterface firmapiud= new FirmapiuDImpl();
		try {
			LOGGER.info(rb.getString("export0")+": "+OBJECTPATH);
			dbusconn.exportObject(OBJECTPATH, firmapiud);
			LOGGER.info(rb.getString("export1"));
		} catch (DBusException e) {
			LOGGER.severe(rb.getString("export3")+": "+OBJECTPATH);
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Il programma non è riuscito ad esportare l'oggetto: rilascia il busname si disconnette ed esce
			try {
				dbusconn.releaseBusName(BUSNAME);
				LOGGER.info(rb.getString("close1")+": "+BUSNAME);
			} catch (DBusException e1) {
				LOGGER.severe(rb.getString("close2")+": "+BUSNAME);
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			dbusconn.disconnect();
			LOGGER.info(rb.getString("close3"));
			System.exit(-1);
		}//fine try-catch
	}//fine main
	
	

}
