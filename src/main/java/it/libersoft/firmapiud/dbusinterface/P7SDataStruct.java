/**
 * 
 */
package it.libersoft.firmapiud.dbusinterface;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;

/**
 * Questa classe descrive una struct (ss) utilizzata da dbus per recuperare il
 * percorso del .p7s e del contenuto originale del file da verificare
 * 
 * @author dellanna
 *
 */
public class P7SDataStruct extends Struct {

	@Position(0)  
	public String p7sFilePath;  
	@Position(1)  
	public String contentFilePath;
	
	public P7SDataStruct(String p7sFilePath, String contentFilePath) {
		super();
		this.p7sFilePath = p7sFilePath;
		this.contentFilePath = contentFilePath;
	}

}
