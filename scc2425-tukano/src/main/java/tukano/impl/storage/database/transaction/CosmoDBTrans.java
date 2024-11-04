package main.java.tukano.impl.storage.database.transaction;

import com.azure.cosmos.models.CosmosBatch;

import java.util.function.Function;

public class CosmoDBTrans implements Transaction <CosmosBatch>{
    public CosmoDBTrans(CosmosBatch b) {
        batch = b;
    }

    private final CosmosBatch batch;
    public <T> T add(Function<CosmosBatch, T> c) {
        return c.apply(batch);
    }

}
