/**
 * 
 */
package firmapiud;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import firmapiu.CommandProxyInterface;
import it.libersoft.FirmapiuDInterface;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * @author andy
 *
 */
class FirmapiuTestClient {

	/**
	 * @param args
	 * @throws DBusException 
	 */
	public static void main(String[] args) throws DBusException {
		// TODO Auto-generated method stub
		System.out.println("Connessione al demone DBUS");
		DBusConnection dbusconn = DBusConnection.getConnection(DBusConnection.SESSION); 
		System.out.println("Connesso...");
		System.out.println("Recupero oggetto remoto");
		FirmapiuDInterface remote = (FirmapiuDInterface) dbusconn.getRemoteObject(  
			FirmapiuD.BUSNAME,FirmapiuD.OBJECTPATH,  
                 FirmapiuDInterface.class); 
		System.out.println("oggetto remoto recuperato");
		
		//String[] paths={"/prova1","/prova2","/prova3","/prova4"};
		//String[] paths={"/prova1"};
		
		//String[] verify=remote.verify(prova);
		
		/*for(String ver: verify)
			System.out.println(ver);*/
		
		//CODICE DI FIRMA---------------------------->
		
		String[] paths={"/prova1"};
		Map<String,Variant<?>> options = new TreeMap<String,Variant<?>>();
		options.put(CommandProxyInterface.PIN, new Variant<Object>("87654321"));
		options.put(CommandProxyInterface.OUTDIR, new Variant<Object>("/home/andy/Scrivania"));
		Map<String,Variant<?>> result=remote.sign(paths, options);
		
		
		Iterator<String> itr=result.keySet().iterator();
		while(itr.hasNext()){
			String key=itr.next();
			System.out.println("Key: "+key);
			Struct struct=(Struct)(result.get(key).getValue());
			Object[] obj=struct.getParameters();
			System.out.println("\tcode:"+obj[0]+" "+" message"+obj[1]);
			System.out.println("Struct -> "+struct);
		}
		
		
		//CODICE DI VERIFICA---------------------------->
		/*String[] paths={"/home/andy/Scrivania/README.txt.p7m","/home/andy/Scrivania/Cose da Fare.txt.p7m","/pippulus"};
		//String[] paths={"/home/andy/Scrivania/t.txt.p7m"};
		Map<String,String> result=remote.verify(paths);
		Iterator<String> itr=result.keySet().iterator();
		while(itr.hasNext()){
			String key=itr.next();
			System.out.println("Key: "+key);
			System.out.println("\t"+result.get(key));
		}
		
		
		/*String str=echo.echo("prova prova prova");
		System.out.println(str);*/
	}

}
