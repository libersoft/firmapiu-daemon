/**
 * 
 */
package it.libersoft.firmapiud.dbusinterface;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;

/**
 * Questa classe realizza una struct utilizzata da dbus per passare al chiamante, in caso di un errore interno alla libreria
 * it.libersoft.firmapiud.dbusinterface.firmapiu, un codice di errore e il messaggio di errore relativo
 * 
 * @author dellanna
 *
 */
public class FirmapiuExceptionStruct extends Struct {
	@Position(0)  
	public final Integer code;  
	@Position(1)  
	public final String message;
	
	
	public FirmapiuExceptionStruct(Integer code, String message) {
		super();
		this.code = code;
		this.message = message;
	}  
}
