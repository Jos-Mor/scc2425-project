package main.java.tukano.impl;

import main.java.utils.Hash;

import java.util.logging.Logger;


public class Token {
	private static Logger Log = Logger.getLogger(Token.class.getName());

	private static final String DELIMITER = "-";
	private static final long MAX_TOKEN_AGE = 10000;
	private static String secret;

	public static void setSecret(String s) {
		secret = s;
	}

	public static String get() {
		var timestamp = System.currentTimeMillis();
		var signature = Hash.of(timestamp, secret);
		return String.format("%s%s%s", timestamp, DELIMITER, signature);
	}
	
	public static String get(String id) {
		var timestamp = System.currentTimeMillis();
		var signature = Hash.of(id, timestamp, secret);
		return String.format("%s%s%s", timestamp, DELIMITER, signature);
	}

	public static boolean isValid(String tokenStr, String id) {
		try {
			var bits = tokenStr.split(DELIMITER);
			var timestamp = Long.valueOf(bits[0]);
			var hmac = Hash.of(id, timestamp, secret);
			var elapsed = Math.abs(System.currentTimeMillis() - timestamp);			
			Log.info(String.format("hash ok:%s, elapsed %s ok: %s\n", hmac.equals(bits[1]), elapsed, elapsed < MAX_TOKEN_AGE));
			return hmac.equals(bits[1]) && elapsed < MAX_TOKEN_AGE;			
		} catch( Exception x ) {
			x.printStackTrace();
			return false;
		}
	}

}
