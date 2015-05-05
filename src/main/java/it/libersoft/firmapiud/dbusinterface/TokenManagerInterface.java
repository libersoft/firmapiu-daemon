/**
 * 
 */
package it.libersoft.firmapiud.dbusinterface;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;

/**
 * @author dellanna
 *
 */
@DBusInterfaceName("it.libersoft.firmapiud.dbusinterface.TokenManagerInterface")
public interface TokenManagerInterface extends DBusInterface {

	//effettua il login sul token crittografico
    public void login (String pin);
        
    public void logout();
    
    //TODO possibile incomaptilità mashalling causa mancata gestione degli unsigned da parte di java
    //restituisce l'atr della carta
    public byte[] getATR();
        
    //setta il nuovo pin del token crittografico
    public void setPin(String oldPin,String newPin);
    
    //setta il nuovo PUK del token crittografico
    public void setPuk(String oldPuk,String newPuk);

    //verifica la correttezza del pin del token crittografico
    public boolean verifyPin(String pin);
    
    //verifica la correttezza del PUK
    public boolean verifyPuk(String puk);
       
    //restituisce il numero di tentativi rimasti prima che il PIN si blocchi
    public int getPinRemainingAttempts();
    
    //restituisce il numero di tentativi rimasti prima che il PUK si blocchi
    public int getPukRemainingAttempts();
    
    //sblocca una carta con il pin bloccata
    public void unlockPKCS11Token(String puk,String newPin);
}
