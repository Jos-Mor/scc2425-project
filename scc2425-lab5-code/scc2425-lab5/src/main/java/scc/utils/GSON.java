package scc.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GSON {
	private static final Gson gson = new Gson();
	
	synchronized public static String encode( Object obj ) {
		try {
			return gson.toJson(obj);
		} catch( Exception x ) {
			x.printStackTrace();
			return null;
		}
	}
	
	
	@SuppressWarnings("unchecked")
	synchronized public static <T> T decode( String json, TypeToken<T> typeTokenT) {
		try {
			return (T)gson.fromJson( json, typeTokenT.getType() );
		} catch( Exception x ) {
			x.printStackTrace();
			return null;
		}
	}
}
