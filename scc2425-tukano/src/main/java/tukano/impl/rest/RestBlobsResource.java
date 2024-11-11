package main.java.tukano.impl.rest;

import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Context;
import main.java.tukano.api.Blobs;

import main.java.tukano.api.rest.RestBlobs;
import main.java.tukano.impl.JavaBlobs;
import jakarta.servlet.http.Cookie;

@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {

	@Context
	HttpServletResponse response;

	final Blobs impl;
	
	public RestBlobsResource() {
		this.impl = JavaBlobs.getInstance();
	}
	
	@Override
	public void upload(String blobId, byte[] bytes, String token, Cookie cookie) {
		super.resultOrThrow( impl.upload(blobId, bytes, token));
	}

	@Override
	public byte[] download(String blobId, String token, Cookie cookie) {
		return super.resultOrThrow( impl.download( blobId, token ));
	}

	@Override
	public void delete(String blobId, String token, Cookie cookie) {
		super.resultOrThrow( impl.delete( blobId, token ));
	}
	
	@Override
	public void deleteAllBlobs(String userId, String password, Cookie cookie) {
		super.resultOrThrow( impl.deleteAllBlobs( userId, password ));
	}
}
