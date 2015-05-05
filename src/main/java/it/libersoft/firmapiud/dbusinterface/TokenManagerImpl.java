/**
 * 
 */
package it.libersoft.firmapiud.dbusinterface;

import it.libersoft.firmapiu.crtoken.PKCS11Token;
import it.libersoft.firmapiu.crtoken.TokenFactoryBuilder;
import it.libersoft.firmapiu.exception.FirmapiuException;
import static it.libersoft.firmapiu.consts.FactoryConsts.*;

import java.util.Locale;
import java.util.ResourceBundle;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

/**
 * @author dellanna
 *
 */
public final class TokenManagerImpl implements TokenManagerInterface {

	//resource bundle di FirmapiuD
	private final ResourceBundle localrb;
	
	/**
	 * 
	 */
	public TokenManagerImpl() {
		ResourceBundle rb = ResourceBundle.getBundle("it.libersoft.firmapiud.lang.locale",Locale.getDefault());
		this.localrb = ResourceBundle.getBundle("it.libersoft.firmapiud.lang.locale",Locale.getDefault());
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
	 * @see it.libersoft.firmapiud.dbusinterface.TokenManagerInterface#login(java.lang.String)
	 */
	@Override
	public void login(String pin) {
		// TODO funzionalità di sessione non supportata: E' possibile che la funzionalità venga supportata in futuro
		throw new DBusExecutionException(localrb.getString("error5"));
	}

	/* (non-Javadoc)
	 * @see it.libersoft.firmapiud.dbusinterface.TokenManagerInterface#logout()
	 */
	@Override
	public void logout() {
		// TODO funzionalità di sessione non supportata: E' possibile che la funzionalità venga supportata in futuro
		throw new DBusExecutionException(localrb.getString("error5"));
	}

	/* (non-Javadoc)
	 * @see it.libersoft.firmapiud.dbusinterface.TokenManagerInterface#getATR()
	 */
	@Override
	public byte[] getATR() {
		//TODO possibile incomaptilità mashalling causa mancata gestione degli unsigned da parte di java
		//Inizializza il pkcs11token
		try {
			PKCS11Token pkcs11Token= TokenFactoryBuilder.getFactory(PKCS11TOKENFACTORY).getPKCS11Token(CRTSMARTCARD);
			return pkcs11Token.getATR();
		}catch (FirmapiuException e) {
			e.printStackTrace();
			throw new DBusExecutionException(e.errorCode+":"+e.getLocalizedMessage());
		}
	}

	/* (non-Javadoc)
	 * @see it.libersoft.firmapiud.dbusinterface.TokenManagerInterface#setPin(java.lang.String, java.lang.String)
	 */
	@Override
	public void setPin(String oldPin, String newPin) {
		//Inizializza il pkcs11token
		try {
			PKCS11Token pkcs11Token= TokenFactoryBuilder.getFactory(PKCS11TOKENFACTORY).getPKCS11Token(CRTSMARTCARD);
			pkcs11Token.setPin(oldPin.toCharArray(), newPin.toCharArray());
		}catch (FirmapiuException e) {
			e.printStackTrace();
			throw new DBusExecutionException(e.errorCode+":"+e.getLocalizedMessage());
		}
	}

	/* (non-Javadoc)
	 * @see it.libersoft.firmapiud.dbusinterface.TokenManagerInterface#setPuk(java.lang.String, java.lang.String)
	 */
	@Override
	public void setPuk(String oldPuk, String newPuk) {
		//Inizializza il pkcs11token
		try {
			PKCS11Token pkcs11Token= TokenFactoryBuilder.getFactory(PKCS11TOKENFACTORY).getPKCS11Token(CRTSMARTCARD);
			pkcs11Token.setPuk(null,oldPuk.toCharArray(), newPuk.toCharArray());
		}catch (FirmapiuException e) {
			e.printStackTrace();
			throw new DBusExecutionException(e.errorCode+":"+e.getLocalizedMessage());
		}
	}

	/* (non-Javadoc)
	 * @see it.libersoft.firmapiud.dbusinterface.TokenManagerInterface#verifyPin(java.lang.String)
	 */
	@Override
	public boolean verifyPin(String pin) {
		// Inizializza il pkcs11token
		try {
			PKCS11Token pkcs11Token= TokenFactoryBuilder.getFactory(PKCS11TOKENFACTORY).getPKCS11Token(CRTSMARTCARD);
			return pkcs11Token.verifyPin(pin.toCharArray());
		}catch (FirmapiuException e) {
			e.printStackTrace();
			throw new DBusExecutionException(e.errorCode+":"+e.getLocalizedMessage());
		}
	}

	/* (non-Javadoc)
	 * @see it.libersoft.firmapiud.dbusinterface.TokenManagerInterface#verifyPuk(java.lang.String)
	 */
	@Override
	public boolean verifyPuk(String puk) {
		// Inizializza il pkcs11token
		try {
			PKCS11Token pkcs11Token= TokenFactoryBuilder.getFactory(PKCS11TOKENFACTORY).getPKCS11Token(CRTSMARTCARD);
			return pkcs11Token.verifyPuk(puk.toCharArray());
		}catch (FirmapiuException e) {
			e.printStackTrace();
			throw new DBusExecutionException(e.errorCode+":"+e.getLocalizedMessage());
		}
	}

	/* (non-Javadoc)
	 * @see it.libersoft.firmapiud.dbusinterface.TokenManagerInterface#getPinRemainingAttempts()
	 */
	@Override
	public int getPinRemainingAttempts() {
		// Inizializza il pkcs11token
		try {
			PKCS11Token pkcs11Token= TokenFactoryBuilder.getFactory(PKCS11TOKENFACTORY).getPKCS11Token(CRTSMARTCARD);
			return pkcs11Token.getPinRemainingAttempts();
		}catch (FirmapiuException e) {
			e.printStackTrace();
			throw new DBusExecutionException(e.errorCode+":"+e.getLocalizedMessage());
		}
	}

	/* (non-Javadoc)
	 * @see it.libersoft.firmapiud.dbusinterface.TokenManagerInterface#getPukRemainingAttempts()
	 */
	@Override
	public int getPukRemainingAttempts() {
		// Inizializza il pkcs11token
		try {
			PKCS11Token pkcs11Token= TokenFactoryBuilder.getFactory(PKCS11TOKENFACTORY).getPKCS11Token(CRTSMARTCARD);
			return pkcs11Token.getPukRemainingAttempts();
		}catch (FirmapiuException e) {
			e.printStackTrace();
			throw new DBusExecutionException(e.errorCode+":"+e.getLocalizedMessage());
		}
	}

	/* (non-Javadoc)
	 * @see it.libersoft.firmapiud.dbusinterface.TokenManagerInterface#unlockPKCS11Token(java.lang.String, java.lang.String)
	 */
	@Override
	public void unlockPKCS11Token(String puk, String newPin) {
		// Inizializza il pkcs11token
		try {
			PKCS11Token pkcs11Token= TokenFactoryBuilder.getFactory(PKCS11TOKENFACTORY).getPKCS11Token(CRTSMARTCARD);
			pkcs11Token.unlockPKCS11Token(puk.toCharArray(), newPin.toCharArray());
		}catch (FirmapiuException e) {
			e.printStackTrace();
			throw new DBusExecutionException(e.errorCode+":"+e.getLocalizedMessage());
		}

	}

}