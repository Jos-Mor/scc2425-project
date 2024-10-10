package main.java.utils;


public class TryCatch {

	public static <T> T maybe( ThrowableSupplier<T> s) {
		try {
			return s.get();
		} catch( Exception x ) {
			throw new RuntimeException(x);
		}
	}
	
	
	public static interface ThrowableSupplier<T> {
		T get() throws Exception ;
	}
}
