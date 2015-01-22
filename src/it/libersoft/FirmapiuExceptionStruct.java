/**
 * 
 */
package it.libersoft;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;

/**
 * Questa classe realizza una struct utilizzata da dbus per passare al chiamante, in caso di un errore interno alla libreria
 * firmapiu, un codice di errore e il messaggio di errore relativo
 * 
 * @author dellanna
 *
 */
class FirmapiuExceptionStruct extends Struct {
	@Position(0)  
	final int code;  
	@Position(1)  
	final String message;
	
	
	FirmapiuExceptionStruct(int code, String message) {
		super();
		this.code = code;
		this.message = message;
	}  
}
