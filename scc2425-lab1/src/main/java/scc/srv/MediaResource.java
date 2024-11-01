package main.java.scc.srv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import io.github.cdimascio.dotenv.DotenvException;
import io.github.cdimascio.dotenv.Dotenv;
import main.java.scc.utils.Hash;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource {
	Map<String, byte[]> map = new HashMap<>();


	// Get connection string in the storage access keys page
	String storageConnectionString = System.getenv("AZURE_KEY");

	// Get container client
	BlobContainerClient containerClient = new BlobContainerClientBuilder()
			.connectionString(storageConnectionString)
			.containerName("images")
			.buildClient();

	/**
	 * Post a new image.The id of the image is its hash.
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {
		var key = Hash.of(contents);
		var blob = containerClient.getBlobClient(key);
		var data = BinaryData.fromBytes(contents);
		blob.upload(data);
		return key;
	}

	/**
	 * Return the contents of an image. Throw an appropriate error message if id
	 * does not exist.
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] download(@PathParam("id") String id) {
		var blob = containerClient.getBlobClient(id);
		byte[] data = blob.downloadContent().toBytes();
		return data;
	}

	/**
	 * Lists the ids of images stored.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> list() {
		return containerClient.listBlobs().stream().map(blobItem -> blobItem.getName()).toList();
	}
}
