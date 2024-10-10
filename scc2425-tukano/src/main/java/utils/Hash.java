package main.java.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
		static MessageDigest md5;
		static MessageDigest sha256;
				
		public static byte[] md5( byte[] data ) {
			if (md5 == null) {
				try {
					md5 = MessageDigest.getInstance("MD5");
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
			md5.reset();
			md5.update( data == null ? new byte[0] : data );
			return md5.digest();
		}
		
		public static byte[] sha256( byte[] data ) {
			if (sha256 == null) {
				try {
					sha256 = MessageDigest.getInstance("SHA-256");
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
			sha256.reset();
			sha256.update( data == null ? new byte[0] : data );
			return sha256.digest();
		}
		
		
		synchronized public static String of(Object ...values) {
			if (md5 == null) {
				try {
					md5 = MessageDigest.getInstance("MD5");
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
			md5.reset();
			for( var o : values )
				md5.update( o.toString().getBytes() );

			return String.format("%016X", new BigInteger(1, md5.digest()));
		};
}
