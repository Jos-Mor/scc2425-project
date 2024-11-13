package main.test.test;

import java.io.File;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Random;

import main.java.tukano.api.Result;
import main.java.tukano.api.TukanoShort;
import main.java.tukano.api.TukanoUser;
import main.java.tukano.impl.rest.TukanoRestServer;
import main.test.tukano.clients.rest.RestBlobsClient;
import main.test.tukano.clients.rest.RestShortsClient;
import main.test.tukano.clients.rest.RestUsersClient;


public class Test {
	
	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}
	
	public static void main(String[] args ) throws Exception {
		new Thread( () -> {	
			try { 
				new TukanoRestServer();
			} catch( Exception x ) {
				x.printStackTrace();
			}
		}).start();

		
		Thread.sleep(1000);
		
		var serverURI = String.format("http://localhost:%s/rest", TukanoRestServer.PORT);
		
		var blobs = new RestBlobsClient(serverURI);
		var users = new RestUsersClient( serverURI);
		var shorts = new RestShortsClient(serverURI);
				
		 show(users.createUser( new TukanoUser("wales", "12345", "jimmy@wikipedia.pt", "Jimmy Wales") ));
		 
		 show(users.createUser( new TukanoUser("liskov", "54321", "liskov@mit.edu", "Barbara Liskov") ));
		 
		 show(users.updateUser("wales", "12345", new TukanoUser("wales", "12345", "jimmy@wikipedia.com", "" ) ));
		 
		 
		 show(users.searchUsers(""));
		
		
		Result<TukanoShort> s1, s2;

		show(s2 = shorts.createShort("liskov", "54321"));		
		show(s1 = shorts.createShort("wales", "12345"));
		show(shorts.createShort("wales", "12345"));
		show(shorts.createShort("wales", "12345"));
		show(shorts.createShort("wales", "12345"));

		var blobUrl = URI.create(s2.value().getBlobUrl());
		System.out.println( "------->" + blobUrl );
		
		var blobId = new File( blobUrl.getPath() ).getName();
		System.out.println( "BlobID:" + blobId );
		
		var token = blobUrl.getQuery().split("=")[1];
		
		blobs.upload(blobUrl.toString(), randomBytes( 100 ), token);

		
		var s2id = s2.value().getShortId();
		
		show(shorts.follow("liskov", "wales", true, "54321"));
		show(shorts.followers("wales", "12345"));
		
		show(shorts.like(s2id, "liskov", true, "54321"));
		show(shorts.like(s2id, "liskov", true, "54321"));
		show(shorts.likes(s2id , "54321"));
		show(shorts.getFeed("liskov", "12345"));
		show(shorts.getShort( s2id ));
		
		show(shorts.getShorts( "wales" ));
		
		show(shorts.followers("wales", "12345"));

		show(shorts.getFeed("liskov", "12345"));

		show(shorts.getShort( s2id ));
//
//		
//		blobs.forEach( b -> {
//			var r = b.download(blobId);
//			System.out.println( Hex.of(Hash.sha256( bytes )) + "-->" + Hex.of(Hash.sha256( r.value() )));
//			
//		});
		
		 show(users.deleteUser("wales", "12345"));

		System.exit(0);
	}
	
	
	static Result<?> show(Result<?> res) {
		if( res.isOK() )
			System.err.println("OK: " + res.value() );
		else
			System.err.println("ERROR:" + res.error());
		return res;
		
	}
	
	private static byte[] randomBytes(int size) {
		var r = new Random(1L);

		var bb = ByteBuffer.allocate(size);
		
		r.ints(size).forEach( i -> bb.put( (byte)(i & 0xFF)));		

		return bb.array();
		
	}
}
