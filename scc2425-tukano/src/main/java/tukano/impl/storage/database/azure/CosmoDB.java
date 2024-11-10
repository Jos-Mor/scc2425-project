package main.java.tukano.impl.storage.database.azure;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.*;
import main.java.tukano.api.Result;
import main.java.tukano.impl.storage.database.azure.CosmosDBSource;
import main.java.tukano.impl.storage.database.imp.DataBase;
import main.java.tukano.impl.storage.database.transaction.CosmoDBTrans;
import main.java.tukano.impl.storage.database.transaction.Transaction;

import static main.java.tukano.impl.rest.TukanoRestServer.Log;

public class CosmoDB implements DataBase<CosmosBatch> {
    private final CosmosContainer container;


    public CosmoDB(Container type) {
        Log.info("start CosmoDB init");
        CosmosDBSource.init();
        container = enumToContainer(type);
        Log.info("finish CosmoDB init");
    }
    public enum Container {
        USERS,
        SHORTS
    }


    private CosmosContainer enumToContainer(Container container) {
        switch (container) {
            case USERS -> {
                return CosmosDBSource.user_container;
            }
            case SHORTS -> {
                return CosmosDBSource.short_container;
            }
        }
        return null;
    }

    public void close() {
        CosmosDBSource.client.close();
    }

    public <T> List<T> sql(String query, Class<T> clazz) {
        return tryCatch(() -> {
            var res = container.queryItems(query, new CosmosQueryRequestOptions(), clazz);
            return res.stream().toList();
        }).value();
    }

    public <T> List<T> sql(String query, Class<T> clazz, Transaction<CosmosBatch> trans) {
        return tryCatch(() -> { //There is no transactional implementation for queryItems using CosmosBatch or any other transactional system available, therefore we pretend there is no transaction
            var res = container.queryItems(query, new CosmosQueryRequestOptions(), clazz);
            return res.stream().toList();
        }).value();
    }

    public <T> Result<T> getOne(String id, Class<T> clazz) {
        return tryCatch( () -> container.readItem(id, new PartitionKey(id), clazz).getItem());
    }

    public <T> Result<T> getOne(String id, Class<T> clazz, Transaction<CosmosBatch> trans) {
        return tryCatch(() -> trans.add(batch -> batch.readItemOperation(id).getItem()));
    }

    public <T> Result<T> deleteOne(T obj) {
        return tryCatch( () -> (T) container.deleteItem( obj, new CosmosItemRequestOptions()).getItem());
    }

    public <T> Result<T> deleteOne(String id, T obj, Transaction<CosmosBatch> trans) {
        return tryCatch(() -> trans.add( batch -> batch.deleteItemOperation(id).getItem()));
    }

    public <T> Result<T> updateOne(T obj) {
        return tryCatch( () -> container.upsertItem(obj).getItem());
    }

    public <T> Result<T> updateOne(T obj, Transaction<CosmosBatch> trans) {
        return tryCatch(() -> trans.add( batch -> batch.upsertItemOperation(obj).getItem()));
    }

    public <T> Result<T> insertOne( T obj) {
        return tryCatch( () -> {
                    var c = container.createItem(obj);
                    Log.info("insertOne: " + c.getStatusCode());
                    var item = c.getItem();
                    Log.info("item: " + item);
                    return item;
                }
        );
    }

    public <T> Result<T> insertOne(T obj, Transaction<CosmosBatch> trans) {
        return tryCatch(() -> trans.add( batch -> batch.createItemOperation(obj).getItem()));
    }


    public <T> Result<T> transaction( Consumer<Transaction<CosmosBatch>> c) {


        CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey("lets pretend this is working"));

        c.accept(new CosmoDBTrans(batch));

        CosmosBatchResponse response = container.executeCosmosBatch(batch);

        if (response.isSuccessStatusCode()) {
            return Result.ok();
        } else {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    <T> Result<T> tryCatch(Supplier<T> supplierFunc) {
        try {
            Log.info("in trycatch");
            return Result.ok(supplierFunc.get());
        } catch( CosmosException ce ) {
            Log.info("cosmos exception");
            ce.printStackTrace();
            return Result.error ( errorCodeFromStatus(ce.getStatusCode() ));
        } catch( Exception x ) {
            Log.info("regular exception");
            x.printStackTrace();
            return Result.error( Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    Result.ErrorCode errorCodeFromStatus( int status ) {
        return switch( status ) {
            case 200 -> Result.ErrorCode.OK;
            case 404 -> Result.ErrorCode.NOT_FOUND;
            case 409 -> Result.ErrorCode.CONFLICT;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }
}
