/**
 * 
 */
package junit;

import static org.junit.Assert.*;
import it.libersoft.firmapiu.exception.FirmapiuException;
import it.libersoft.firmapiud.dbusinterface.FirmapiuDInterface;
import it.libersoft.firmapiud.dbusinterface.TokenManagerInterface;

import org.freedesktop.dbus.DBusConnection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.Log4jLoggerAdapter;

import it.libersoft.firmapiu.exception.*;

/**
 * Semplice Test per la gestione del token crittografico
 * 
 * @author dellanna
 *
 */
public class TokenManagerTest {

	//setta il logger slf4 su Log4j
	private static Log4jLoggerAdapter Log;
	
	//carica oggetto per la gestione di un token crittografico
	private final static String BUSNAME="it.libersoft.firmapiud.dbusinterface.TokenManagerInterface";
	private final static String OBJECTPATH="/it/libersoft/firmapiud/TokenManager";
	
	//PIN e PUK di test. Attenzione PIN e PUK sbagliati possono bloccare la carta
	private final static String OLDPUK="87654321"; 
	private final static String OLDPIN="87654321";
	private final static String NEWPUK="12345678";
	private final static String NEWPIN="12345678";
	
	//un pin/puk volutamente sbagliato
	private final static String WRONGCODE="46375649";
	
	//intefaccia oggetto remoto e a DBUS
	private static TokenManagerInterface tokenInterface=null;
	private static DBusConnection dbusconn=null;
	
	//flag di sicurezza: imposta il flag a false se uno dei test fallisce in modo da far fallire anche gli altri
	//in modo da non far bloccare la carta
	private static boolean securityFlag=true;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//setta il logger slf4 su Log4j
		Log = (Log4jLoggerAdapter)LoggerFactory.getLogger(TokenManagerTest.class);
		Log.info("Connessione al demone DBUS: Attenzione il demone deve essere attivo per effettuare il test.");
		dbusconn = DBusConnection.getConnection(DBusConnection.SESSION); 
		Log.info("Connesso...");
		Log.info("Recupero oggetto remoto");
		tokenInterface = (TokenManagerInterface) dbusconn.getRemoteObject( BUSNAME,OBJECTPATH, TokenManagerInterface.class); 
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
		if (tokenInterface==null)
			fail("Il test è fallito poiché la token interface non è stata inizializzata");
		if(!securityFlag)
			fail("Il test è fallito per motivi di sicurezza!");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link it.libersoft.firmapiud.dbusinterface.TokenManagerImpl#getATR()}.
	 */
	@Test
	public final void testGetATR() {
		try {
			Log.info("Testo il recupero dell'ATR dalla carta");
			byte[] atr=tokenInterface.getATR();
			String atrStr="";
			for (byte element : atr) {
				atrStr+=":"+Integer.toHexString(element);
			}
			Log.info("ATR: {}",atrStr);
		} catch (Exception e) {
			securityFlag=false;
			throw e;
		}
	}

	/**
	 * Test method for {@link it.libersoft.firmapiud.dbusinterface.TokenManagerImpl#setPin(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testSetPin() {
		try {
			Log.info("Testo il cambio del PIN della carta");
			tokenInterface.setPin(OLDPIN, NEWPIN);
			Log.info("Cambio Pin effettuato correttamente: ritorno al vecchio PIN");
			tokenInterface.setPin(NEWPIN, OLDPIN);
		} catch (Exception e) {
			securityFlag=false;
			throw e;
		}
	}

	/**
	 * Test method for {@link it.libersoft.firmapiud.dbusinterface.TokenManagerImpl#setPuk(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testSetPuk() {
		try {
			Log.info("Testo il cambio del PUK della carta");
			tokenInterface.setPuk(OLDPUK, NEWPUK);
			Log.info("Cambio Puk effettuato correttamente: ritorno al vecchio PUK");
			tokenInterface.setPuk(NEWPUK, OLDPUK);
		} catch (Exception e) {
			securityFlag=false;
			throw e;
		}
	}

	/**
	 * Test method for {@link it.libersoft.firmapiud.dbusinterface.TokenManagerImpl#verifyPin(java.lang.String)}.
	 */
	@Test
	public final void testVerifyPin() {
		Log.info("Testa volutamente un pin sbagliato");
		try {
			boolean code=tokenInterface.verifyPin(WRONGCODE);
			assertFalse("Attenzione il PIN dovrebbe essere sbagliato!", code);
		} catch (Exception e) {
			String errorCode=FirmapiuException.getDefaultErrorCodeMessage(FirmapiuException.CRT_TOKENPINPUK_VERIFY_ERROR);
			if (e.getLocalizedMessage().contains(errorCode))
				Log.info("E' stato rilevato 'correttamente' un errore: {}",e.getLocalizedMessage());
			else
			{
				securityFlag=false;
				throw e;
			}
		}
		Log.info("Testa il pin corretto");
		try {
			boolean code=tokenInterface.verifyPin(OLDPIN);
			assertTrue("Attenzione il Pin dovrebbe essere corretto!",code);
		} catch (Exception e) {
			securityFlag=false;
			throw e;
		}
	}

	/**
	 * Test method for {@link it.libersoft.firmapiud.dbusinterface.TokenManagerImpl#verifyPuk(java.lang.String)}.
	 */
	@Test
	public final void testVerifyPuk() {
		Log.info("Testa volutamente un puk sbagliato");
		try {
			boolean code=tokenInterface.verifyPuk(WRONGCODE);
			assertFalse("Attenzione il PUK dovrebbe essere sbagliato!", code);
		} catch (Exception e) {
			String errorCode=FirmapiuException.getDefaultErrorCodeMessage(FirmapiuException.CRT_TOKENPINPUK_VERIFY_ERROR);
			if (e.getLocalizedMessage().contains(errorCode))
				Log.info("E' stato rilevato 'correttamente' un errore: {}",e.getLocalizedMessage());
			else
			{
				securityFlag=false;
				throw e;
			}
		}
		Log.info("Testa il puk corretto");
		try {
			boolean code=tokenInterface.verifyPuk(OLDPUK);
			assertTrue("Attenzione il Puk dovrebbe essere corretto!",code);
		} catch (Exception e) {
			securityFlag=false;
			throw e;
		}
	}

	/**
	 * Test method for {@link it.libersoft.firmapiud.dbusinterface.TokenManagerImpl#getPinRemainingAttempts()}.
	 */
	@Test
	public final void testGetPinRemainingAttempts() {
		int expectedAttempts=3;
		try {
			Log.info("Testa quanti tentativi PIN sono rimasti prima di bloccare la carta");
			Log.info("Si aspetta {} tentativi",expectedAttempts);
			tokenInterface.verifyPin(OLDPIN);
			int actualAttempts=tokenInterface.getPinRemainingAttempts();
			Log.debug("Tentativi "+actualAttempts);
			assertEquals(new Integer(expectedAttempts), new Integer(actualAttempts));
			Log.info("Sbaglia volutamente il pin per diminuire di uno i tentativi rimasti");
			expectedAttempts--;
			Log.info("Si aspetta {} tentativi",expectedAttempts);
			try {
				tokenInterface.verifyPin(WRONGCODE);
			} catch (Exception e) {
			}
			actualAttempts=tokenInterface.getPinRemainingAttempts();
			assertEquals(new Integer(expectedAttempts), new Integer(actualAttempts));
			Log.info("Verifica correttamente il PIN per ritornare al numero di tentativi originali");
			expectedAttempts=3;
			Log.info("Si aspetta {} tentativi",expectedAttempts);
			try {
				tokenInterface.verifyPin(OLDPIN);
			} catch (Exception e) {
			}
			actualAttempts=tokenInterface.getPinRemainingAttempts();
			assertEquals(new Integer(expectedAttempts), new Integer(actualAttempts));
		} catch (Exception e) {
			securityFlag=false;
			throw e;
		}
	}

	/**
	 * Test method for {@link it.libersoft.firmapiud.dbusinterface.TokenManagerImpl#getPukRemainingAttempts()}.
	 */
	@Test
	public final void testGetPukRemainingAttempts() {
		int expectedAttempts=0;
		try {
			Log.info("Testa quanti tentativi PUK sono rimasti prima di bloccare la carta");
			Log.info("Si aspetta tentativi>{}",expectedAttempts);
			tokenInterface.verifyPuk(OLDPUK);
			int actualAttempts=tokenInterface.getPukRemainingAttempts();
			Log.info("Tentativi "+actualAttempts);
			assertTrue(actualAttempts>0);
			Log.info("Sbaglia volutamente il pin per diminuire di uno i tentativi rimasti");
			expectedAttempts=actualAttempts-1;
			Log.info("Si aspetta {} tentativi",expectedAttempts);
			try {
				tokenInterface.verifyPuk(WRONGCODE);
			} catch (Exception e) {
			}
			actualAttempts=tokenInterface.getPukRemainingAttempts();
			assertEquals(new Integer(expectedAttempts), new Integer(actualAttempts));
			Log.info("Verifica correttamente il PIN per ritornare al numero di tentativi originali");
			tokenInterface.verifyPuk(OLDPUK);
		} catch (Exception e) {
			securityFlag=false;
			throw e;
		}
	}

	/**
	 * Test method for {@link it.libersoft.firmapiud.dbusinterface.TokenManagerImpl#unlockPKCS11Token(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testUnlockPKCS11Token() {
		try{
			Log.info("Testa la funzionalità di sblocco del PIN:");
			Log.info("Blocca la carta sbagliando più di tre volte il PIN");
			try {
				tokenInterface.verifyPin(WRONGCODE);
				tokenInterface.verifyPin(WRONGCODE);
				tokenInterface.verifyPin(WRONGCODE);
			} catch (Exception e1) {
			}
			try {
				tokenInterface.verifyPin(WRONGCODE);
			} catch (Exception e) {
				Log.info("La carta è bloccata");
			}
			tokenInterface.unlockPKCS11Token(OLDPUK, OLDPIN);
			Log.info("Controlla che la carta sia stata sbloccata");
			tokenInterface.verifyPin(OLDPIN);
			Log.info("La carta è stata sbloccata");
		} catch (Exception e) {
			securityFlag=false;
			throw e;
		}
	}
}
