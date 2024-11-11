package main.java.tukano.impl.storage.blobs;

import java.util.function.Consumer;

import main.java.tukano.api.Result;

public interface BlobStorage {
		
	Result<Void> write(String path, byte[] bytes);
		
	Result<Void> delete(String path);
	
	Result<byte[]> read(String path);

	Result<Void> read(String path, Consumer<byte[]> sink);

}
