package main.java.utils;

import java.math.BigInteger;

public class Random {
	private static final java.util.Random rg = new java.util.Random();
	private static final int RADIX = 32;
	
	public static String key128() {
		return new BigInteger(128, rg).toString( RADIX );
	}
}
