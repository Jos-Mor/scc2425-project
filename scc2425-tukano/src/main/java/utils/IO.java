package main.java.utils;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Consumer;

final public class IO {


	public static void write( File out, byte[] data ) {
		try {
			System.out.println("WRITE>>>>" + out);
			
			Files.write( out.toPath(), data);
		} catch( Exception x ) {
			x.printStackTrace();
		}
	}

	public static byte[] read( File from) {
		try {
			System.out.println("READ>>>>" + from);
			return Files.readAllBytes( from.toPath() );
		} catch( Exception x ) {
			x.printStackTrace();
			return null;
		}
	}
	
	public static void read( File from, int chunkSize, Consumer<byte[]> sink) {
		try (var fis = new FileInputStream(from)) {
			int n;
			var chunk = new byte[chunkSize];
			while ((n = fis.read(chunk)) > 0)
				sink.accept(Arrays.copyOf(chunk, n));
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}
	
	public static boolean delete( File file) {
		try {
			if( file.exists() ) {
				Files.delete( file.toPath() );
				return true;
			}
		} catch( Exception x ) {
			x.printStackTrace();
		}
		return false;
	}
	
	public static void write( OutputStream out, char data ) {
		try {
			out.write( data );			
		} catch( IOException x ) {
			x.printStackTrace();
		}
	}
	
	public static void write( OutputStream out, byte[] data ) {
		try {
			out.write( data );			
		} catch( IOException x ) {
			x.printStackTrace();
		}
	}
	
	public static void write( OutputStream out, byte[] data, int off, int len ) {
		try {
			out.write( data, off, len );			
		} catch( IOException x ) {
			x.printStackTrace();
		}
	}
	
	public static String readLine( BufferedReader reader ) {
		try {
			return reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] readAllBytes( InputStream in ) {	
		try(var is = in){
			return is.readAllBytes();
		} catch(IOException x) {
			throw new RuntimeException( x );
		}
	}	
	
	public static void close( Closeable c ) {
		try {
			c.close();
		} catch (IOException e) {
			throw new RuntimeException( e );
		}
	}
}
