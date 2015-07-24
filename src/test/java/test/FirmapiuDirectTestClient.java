/**
 * 
 */
package test;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import it.libersoft.firmapiu.cades.CommandProxyInterface;
import it.libersoft.firmapiud.FirmapiuD;
import it.libersoft.firmapiud.dbusinterface.FirmapiuDInterface;
import it.libersoft.firmapiud.dbusinterface.FirmapiuExceptionStruct;
import it.libersoft.firmapiud.dbusinterface.P7SDataStruct;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DirectConnection;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * @author andy
 *
 */
class FirmapiuDirectTestClient {

	final static String BUSNAME="it.libersoft.firmapiud.dbusinterface.FirmapiuDInterface";
	final static String OBJECTPATH="/it/libersoft/firmapiud/FirmapiuD";
	
	/**
	 * @param args
	 * @throws DBusException 
	 * @throws ParseException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws DBusException, ParseException, InterruptedException {
		// TODO Auto-generated method stub
		System.out.println("Connessione diretta a:"+args[0]);
		DirectConnection dbusconn = new DirectConnection("unix:abstract="+args[0]); 
		System.out.println("Connesso...");
		System.out.println("Recupero oggetto remoto");
		System.out.println(dbusconn.getAddress().toString());
		Thread.sleep(5000);
		FirmapiuDInterface remote = (FirmapiuDInterface) dbusconn.getRemoteObject( OBJECTPATH, FirmapiuDInterface.class); 
		System.out.println("oggetto remoto recuperato");
		
		//String[] paths={"/prova1","/prova2","/prova3","/prova4"};
		//String[] paths={"/prova1"};
		
		//String[] verify=remote.verify(prova);
		
		/*for(String ver: verify)
			System.out.println(ver);*/
		
		//CODICE DI FIRMA---------------------------->
		
		//String[] paths={"/prova1","/home/andy/it.libersoft.firmapiud.dbusinterface.firmapiu/README.txt"};
//		Variant<String> str = new Variant<String>("/home/andy/puppaflex.txt","s");
//		Variant<String> str2 = new Variant<String>("/home/andy/overview.html","s");
//		Variant<?>[] paths={str,str2};
//
//		Map<String,Variant<?>> options = new TreeMap<String,Variant<?>>();
//		options.put(CommandProxyInterface.PIN, new Variant<Object>("12345678"));
//		options.put(CommandProxyInterface.OUTDIR, new Variant<Object>("/home/andy/Scrivania"));
//		Map<String,Variant<?>> result=remote.sign(paths, options);
//		
//		
//		Iterator<String> itr=result.keySet().iterator();
//		while(itr.hasNext()){
//			String key=itr.next();
//			System.out.println("Key: "+key);
//			Variant<?> value=result.get(key);
//			String sig=value.getSig();
//			if(sig.equals("s"))
//				System.out.println(key+" --> "+(String)value.getValue());
//			else if (sig.equals("(is)"))
//			{
//				Struct struct=(Struct)(result.get(key).getValue());
//				Object[] obj=struct.getParameters();
//				System.out.println("\tcode:"+obj[0]+" "+" message"+obj[1]);
//				System.out.println("Struct -> "+struct);
//			}
//			else 
//				System.out.println("non ho capito la risposta");
//		}
		
		//CODICE DI VERIFICA p7s---------------------------->
		P7SDataStruct p7sdata = new P7SDataStruct("/home/andy/prova.p7s", "/home/andy/overview.html");
		Variant<P7SDataStruct> varData = new Variant<P7SDataStruct>(p7sdata,P7SDataStruct.class);
		Map<String,Variant<?>> options = new TreeMap<String,Variant<?>>();
		options.put("detached", new Variant<Boolean>(new Boolean(true),"b"));
		Map<String,Variant<?>>[] result=remote.verifySingle(varData, options);
		
		Iterator<String> itr2=result[0].keySet().iterator();
		while(itr2.hasNext()){
			String key=itr2.next();
			System.out.println("Key: "+key);
			System.out.println("\t"+result[0].get(key));
		}
//		//CODICE DI VERIFICA---------------------------->
//		//String[] paths={"/home/andy/Scrivania/README.txt.p7m","/home/andy/Scrivania/Cose da Fare.txt.p7m","/pippulus"};
//		//String[] paths={"/home/andy/Scrivania/t.txt.p7m"};
//
//		//String[] paths={"/home/andy/Scrivania/README.txt.p7m","/home/andy/Scrivania/Cose da Fare.txt.p7m","/pippulus"};
//		//String[] paths={"/home/andy/Scrivania/t.txt.p7m"};
//		Variant<String> str1 = new Variant<String>("/home/andy/Scrivania/README.txt.p7m","s");
//		Variant<String> str2 = new Variant<String>("/home/andy/Scrivania/Cose da Fare.txt.p7m","s");
//		Variant<String> str3 = new Variant<String>("/Prova","s");
//		Variant<?>[] paths2={str1,str2,str3};
//		
//		Map<String,String> result2=remote.verify(paths2);
//		Iterator<String> itr2=result2.keySet().iterator();
//		while(itr2.hasNext()){
//			String key=itr2.next();
//			System.out.println("Key: "+key);
//			System.out.println("\t"+result2.get(key));
//		}
//		
//		
//		/*String str=echo.echo("prova prova prova");
//		System.out.println(str);*/

//		//CODICE DI RECUPERO CONTENUTO ORIGINALE FILE---------------------------->
//		Variant<String> str1 = new Variant<String>("/home/andy/Scrivania/p7mfiles2/README.txt.p7m","s");
//		//Variant<String> str1 = new Variant<String>("/home/andy/Scrivania/README.txt.p7m","s");
//		//Variant<String> str2 = new Variant<String>("/home/andy/Scrivania/Cose da Fare.txt.p7m","s");
//		Variant<String> str2 = new Variant<String>("/home/andy/Scrivania/p7mfiles2/Cose da Fare.txt.p7m","s");
//		Variant<String> str3 = new Variant<String>("/Prova.p7m","s");
//		Variant<?>[] paths2={str1,str2,str3};
//		
//		Map<String,Variant<?>> options = new TreeMap<String,Variant<?>>();
//		options.put("outdir", new Variant<String>("/tmp"));
//		options.put("copyfile", new Variant<Boolean>(true));
//		
//		Map<String,Variant<?>> result=remote.getContentSignedData(paths2, options);
//		
//		Iterator<String> itr=result.keySet().iterator();
//		while(itr.hasNext()){
//			String key=itr.next();
//			System.out.println("Key: "+key);
//			Variant<?> value=result.get(key);
//			String sig=value.getSig();
//			if(sig.equals("s"))
//				System.out.println(key+" --> "+(String)value.getValue());
//			else if (sig.equals("(is)"))
//			{
//				Object[] obj=(Object[])result.get(key).getValue();
//				//Object[] obj=struct.getParameters();
//				System.out.println("\tcode:"+obj[0]+" "+" message"+obj[1]);
//				//System.out.println("Struct -> "+struct);
//			}
//			else 
//				System.out.println("non ho capito la risposta");
//		}
	
	}

}
