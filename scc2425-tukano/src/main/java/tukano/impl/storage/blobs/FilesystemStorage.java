package main.java.tukano.impl.storage.blobs;



import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

import main.java.tukano.api.Result;
import main.java.utils.Hash;
import main.java.utils.IO;


import static main.java.tukano.api.Result.*;
import static main.java.tukano.api.Result.ErrorCode.*;


public class FilesystemStorage implements BlobStorage {
	private final String rootDir;
	private static final int CHUNK_SIZE = 4096;
	private static final String DEFAULT_ROOT_DIR = "/tmp/";

	public FilesystemStorage() {
		this.rootDir = DEFAULT_ROOT_DIR;
	}
	
	@Override
	public Result<Void> write(String path, byte[] bytes) {
		if (path == null)
			return error(BAD_REQUEST);

		var file = toFile( path );

		if (file.exists()) {
			if (Arrays.equals(Hash.sha256(bytes), Hash.sha256(IO.read(file))))
				return ok();
			else
				return error(CONFLICT);

		}
		IO.write(file, bytes);
		return ok();
	}

	@Override
	public Result<byte[]> read(String path) {
		if (path == null)
			return error(BAD_REQUEST);
		
		var file = toFile( path );
		if( ! file.exists() )
			return error(NOT_FOUND);
		
		var bytes = IO.read(file);
		return bytes != null ? ok( bytes ) : error( INTERNAL_ERROR );
	}

	@Override
	public Result<Void> read(String path, Consumer<byte[]> sink) {
		if (path == null)
			return error(BAD_REQUEST);
		
		var file = toFile( path );
		if( ! file.exists() )
			return error(NOT_FOUND);
		
		IO.read( file, CHUNK_SIZE, sink );
		return ok();
	}
	
	@Override
	public Result<Void> delete(String path) {
		if (path == null)
			return error(BAD_REQUEST);

		try {
			var file = toFile( path );
			Files.walk(file.toPath())
			.sorted(Comparator.reverseOrder())
			.map(Path::toFile)
			.forEach(File::delete);
		} catch (IOException e) {
			e.printStackTrace();
			return error(INTERNAL_ERROR);
		}
		return ok();
	}
	
	private File toFile(String path) {
		var res = new File( rootDir + path );
		
		var parent = res.getParentFile();
		if( ! parent.exists() )
			parent.mkdirs();
		
		return res;
	}

	
}
