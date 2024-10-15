package main.java.tukano.impl.storage;

import java.util.function.Consumer;

import java.util.HashMap;
import java.util.Map;
import main.java.utils.Hash;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;


import main.java.tukano.api.Result;

import static main.java.tukano.api.Result.ErrorCode.*;
import static main.java.tukano.api.Result.error;
import static main.java.tukano.api.Result.ok;

public class AzureStorage implements BlobStorage {
    Map<String, byte[]> map = new HashMap<>();


    // Get connection string in the storage access keys page
    String storageConnectionString = System.getenv("AZURE_KEY");

    // Get container client
    BlobContainerClient containerClient = new BlobContainerClientBuilder()
            .connectionString(storageConnectionString)
            .containerName("blobs")
            .buildClient();


    @Override
    public Result<Void> write(String path, byte[] bytes) {
        if (path == null) {
            return error(BAD_REQUEST);
        }
        var blob = containerClient.getBlobClient(path);
        if (blob.exists()) {
            return error(CONFLICT);
        }
        var data = BinaryData.fromBytes(bytes);
        blob.upload(data);
        if (blob.exists()) {
            return ok();
        } else {
            return error(INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> delete(String path) {
        if (path == null) {
            return error(BAD_REQUEST);
        }
        var blob = containerClient.getBlobClient(path);
        if (blob.exists()) {
            blob.delete();
            return ok();
        } else {
            return error(NOT_FOUND);
        }
    }

    @Override
    public Result<byte[]> read(String path) {
        if (path == null)
            return error(BAD_REQUEST);

        var blob = containerClient.getBlobClient(path);
        if (!blob.exists()) {
            return error(NOT_FOUND);
        }

        byte[] data = blob.downloadContent().toBytes();
        return data != null ? ok(data) : error(INTERNAL_ERROR);
    }

    @Override
    public Result<Void> read(String path, Consumer<byte[]> sink) {
        if (path == null)
            return error(BAD_REQUEST);

        var blob = containerClient.getBlobClient(path);
        if (!blob.exists()) {
            return error(NOT_FOUND);
        }

        byte[] data = blob.downloadContent().toBytes();
        if (data != null) {
            sink.accept(data);
            return ok();
        } else {
            return error(INTERNAL_ERROR);
        }
    }
}
