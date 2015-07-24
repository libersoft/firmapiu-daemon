/**
 * 
 */
package it.libersoft.firmapiud;

import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DirectConnection;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * Finalizzatore per la chiusura di FirmapiuD in caso di chiusura forzata
 * (Caso connessione diretta)
 * 
 * @author dellanna
 *
 */
class DirectConnectionFinalizer extends Thread {

	private final DirectConnection directConn;
	private final ResourceBundle rb;
	private final Logger logger;
	
	
	DirectConnectionFinalizer(DirectConnection dbusconn,ResourceBundle rb) {
		super();
		this.directConn = dbusconn;
		this.rb=rb;
		logger=Logger.getLogger(this.getClass().getCanonicalName());
	}



	@Override
	public void run() {
		//cerca di chiudere la connessione su dbus in caso di chiusura forzata dell'applicazione
		logger.info(rb.getString("forceclose0"));
		//cerca di rilasciare il dbusname di Firmapiud e Token Manager
		directConn.disconnect();
		logger.info(rb.getString("close3"));
	}
}
