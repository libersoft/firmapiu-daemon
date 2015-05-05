/**
 * 
 */
package it.libersoft.firmapiud;

import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * Finalizzatore per la chiusura di FirmapiuD in caso di chiusura forzata
 * 
 * @author dellanna
 *
 */
class FirmapiuDFinalizer extends Thread {

	private final DBusConnection dbusconn;
	private final ResourceBundle rb;
	private final Logger logger;
	
	
	FirmapiuDFinalizer(DBusConnection dbusconn,ResourceBundle rb) {
		super();
		this.dbusconn = dbusconn;
		this.rb=rb;
		logger=Logger.getLogger(this.getClass().getCanonicalName());
	}



	@Override
	public void run() {
		//cerca di chiudere la connessione su dbus in caso di chiusura forzata dell'applicazione
		logger.info(rb.getString("forceclose0"));
		//cerca di rilasciare il dbusname di Firmapiud e Token Manager
		try {
			dbusconn.releaseBusName(FirmapiuD.BUSNAME1);
			logger.info(rb.getString("close1")+": "+FirmapiuD.BUSNAME1);
		} catch (DBusException e) {
			logger.severe(rb.getString("close2")+": "+FirmapiuD.BUSNAME1);
			e.printStackTrace();
		}
		try {
			dbusconn.releaseBusName(FirmapiuD.BUSNAME2);
			logger.info(rb.getString("close1")+": "+FirmapiuD.BUSNAME2);
		} catch (DBusException e) {
			logger.severe(rb.getString("close2")+": "+FirmapiuD.BUSNAME2);
			e.printStackTrace();
		}
		dbusconn.disconnect();
		logger.info(rb.getString("close3"));
	}
	
	
}
