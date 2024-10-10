package main.java.utils;

import java.util.Arrays;

public class Props {

	public static void load(String[] keyValuePairs) {
		System.out.println(Arrays.asList( keyValuePairs));
		for( var pair: keyValuePairs ) {
			var parts = pair.split("=");
			if( parts.length == 2) 
				System.setProperty(parts[0], parts[1]);
		}
	}
}
