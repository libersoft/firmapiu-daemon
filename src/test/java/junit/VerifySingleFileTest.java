/**
 * 
 */
package junit;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.TreeMap;

import it.libersoft.firmapiud.dbusinterface.FirmapiuDInterface;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.Variant;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.Log4jLoggerAdapter;

/**
 * Semplice test per testare la funzionalità di verifica
 * 
 * @author dellanna
 *
 */
public class VerifySingleFileTest {

	//setta il logger slf4 su Log4j
	private static Log4jLoggerAdapter Log;
	
	//carica oggetto per la gestione della busta crittografica
	final static String BUSNAME="it.libersoft.firmapiud.dbusinterface.FirmapiuDInterface";
	final static String OBJECTPATH="/it/libersoft/firmapiud/FirmapiuD";
	
	//intefaccia oggetto remoto e a DBUS
	private static FirmapiuDInterface firmapiuDInterface=null;
	private static DBusConnection dbusconn=null;
		
	//File da verificare
	private static String VERIFYFILE1="/home/andy/Scrivania/p7mfiles2/README.txt.p7m";
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//setta il logger slf4 su Log4j
		Log = (Log4jLoggerAdapter)LoggerFactory.getLogger(VerifySingleFileTest.class);
		Log.info("Connessione al demone DBUS: Attenzione il demone deve essere attivo per effettuare il test.");
		dbusconn = DBusConnection.getConnection(DBusConnection.SESSION); 
		Log.info("Connesso...");
		Log.info("Recupero oggetto remoto");
		firmapiuDInterface = (FirmapiuDInterface) dbusconn.getRemoteObject( BUSNAME,OBJECTPATH, FirmapiuDInterface.class); 
		Log.info("oggetto remoto recuperato");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Log.info("Fine batteria di test: disconnessione dal Bus");
		dbusconn.disconnect();
		Log.info("Disconnesso!");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		if (firmapiuDInterface==null)
			fail("Il test è fallito poiché la firmapiuD interface non è stata inizializzata");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link it.libersoft.firmapiud.dbusinterface.FirmapiuDImpl#verifySingle(org.freedesktop.dbus.Variant, java.util.Map)}.
	 */
	@Test
	public final void testVerifySingle() {
		Log.info("Effettuo il test di verifica sul file {}",VERIFYFILE1);
		Variant<String> file = new Variant<String>(VERIFYFILE1,"s");
		Object[] result=firmapiuDInterface.verifySingle(file, new TreeMap<String,Variant<?>>());
	}
}
