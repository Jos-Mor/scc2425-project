package main.java.tukano.impl.rest.utils;

import main.java.utils.JSON;

import java.io.InputStreamReader;
import java.util.Properties;

import static main.java.tukano.impl.rest.TukanoRestServer.Log;

public class Props {

	public static String get(String key, String defaultValue) {
		var val = System.getProperty(key);
		return val == null ? defaultValue : val;
	}
	
	public static <T> T get(String key, Class<T> clazz) {
		var val = System.getProperty(key);
		if( val == null )
			return null;		
		return JSON.decode(val, clazz);
	}
	
	public static void load( String resourceFile ) {		
		try( var in = Props.class.getClassLoader().getResourceAsStream(resourceFile) ) {			 
			var reader = new InputStreamReader(in);
			var props = new Properties();
		    props.load(reader);

			props.forEach( (k,v) -> {
				System.setProperty(k.toString(), v.toString());
				Log.info(k.toString() + v.toString());
			} );
			System.getenv().forEach( System::setProperty );
		}
		catch( Exception x  ) {
			x.printStackTrace();
		}

	}
}
