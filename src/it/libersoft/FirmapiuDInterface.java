/**
 * 
 */
package it.libersoft;

import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;
import org.freedesktop.dbus.Variant;

/**
 * @author dellanna
 *
 */


@DBusInterfaceName("it.libersoft.FirmapiuDInterface") 
//@DBusInterfaceName("org.freedesktop.FirmapiuDInterface")
public interface FirmapiuDInterface extends DBusInterface {
	
	//firma i file passati come parametro
	public Map<String,Variant<?>> sign (Variant<?>[] args,Map<String,Variant<?>> options);
	
	//verifica la firma dei file passati come parametro
	public Map<String,String> verify (String[] args);

}
