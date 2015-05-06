/**
 * 
 */
package it.libersoft.firmapiud.dbusinterface;

import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;
import org.freedesktop.dbus.Variant;

/**
 * @author dellanna
 *
 */


@DBusInterfaceName("it.libersoft.firmapiud.dbusinterface.FirmapiuDInterface") 
//@DBusInterfaceName("org.freedesktop.FirmapiuDInterface")
public interface FirmapiuDInterface extends DBusInterface {
	
	//firma i file passati come parametro
	public Map<String,Variant<?>> sign (Variant<?>[] args,Map<String,Variant<?>> options);
	
	//verifica la firma dei file passati come parametro
	public Map<String,String> verify (Variant<?>[] args);
	
	//verifica un singolo file passato come parametro: restituisce un array di dictionaries
	//contenente l'esito dell'operazione di verifica per ogni firmatario
	public Map<String,Variant<?>>[] verifySingle (Variant<?> arg, Map<String,Variant<?>> options);

	//restituisce l'originale di un file contenuto in una busta crittografica  p7m 
	public Map<String,Variant<?>> getContentSignedData(Variant<?>[] args,Map<String,Variant<?>> options);
}
