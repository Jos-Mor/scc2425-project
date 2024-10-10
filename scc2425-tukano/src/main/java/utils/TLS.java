package main.java.utils;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class TLS {
	static final String KEYSTORE_PROP_KEY = "javax.net.ssl.keyStore";
	static final String TRUSTSTORE_PROP_KEY = "javax.net.ssl.trustStore";
	static final String KEYSTORE_PASS_PROP_KEY = "javax.net.ssl.keyStorePassword";
	static final String TRUSTSTORE_PASS_PROP_KEY = "javax.net.ssl.trustStorePassword";
	
	public static TrustManagerFactory trustStore() {
		try {
			var trustStoreFile = System.getProperty(TRUSTSTORE_PROP_KEY);
			var trutStorePassword = System.getProperty(TRUSTSTORE_PASS_PROP_KEY);

			var trustStore = keyStore( trustStoreFile, trutStorePassword);
			var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(trustStore);	
			return trustManagerFactory;
		} catch( Exception x ) {
			x.printStackTrace();
			throw new RuntimeException(x);
		}		
	}
	
	
	public static KeyManagerFactory keyStore() {
			try {
				var keyStoreFile = System.getProperty(KEYSTORE_PROP_KEY);
				var keyStorePassword = System.getProperty(KEYSTORE_PASS_PROP_KEY);

				var keyStore = keyStore( keyStoreFile, keyStorePassword);
				var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
				return keyManagerFactory;
			} catch( Exception x ) {
				x.printStackTrace();
				throw new RuntimeException(x);
			}
		}
		
		
	private static KeyStore keyStore(String filename, String password) throws Exception {
		var keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		try (var in = new FileInputStream(filename)) {
			keystore.load(in, password.toCharArray());
		}
		return keystore;
	}
}
