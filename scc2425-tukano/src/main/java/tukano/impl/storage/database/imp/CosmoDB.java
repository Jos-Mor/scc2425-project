package main.java.tukano.impl.storage.database.imp;
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
import main.java.tukano.impl.storage.database.transaction.CosmoDBTrans;
import main.java.tukano.impl.storage.database.transaction.Transaction;

public class CosmoDB extends CosmosDBSource implements DataBase<CosmosBatch>{

    public CosmoDB(Container type) {
        super();
        container = enumToContainer(type);
        setPartitionKey(type);
    }
    public enum Container {
        USERS,
        SHORTS
    }


    private CosmosContainer enumToContainer(Container container) {
        switch (container) {
            case USERS -> {
                return user_container;
            }
            case SHORTS -> {
                return short_container;
            }
        }
        return null;
    }

    private void setPartitionKey(Container container) {
        switch (container) {
            case USERS -> {
                partitionKey = new PartitionKey("userId");
                break;
            }
            case SHORTS -> {
                partitionKey = new PartitionKey("shortId");
                break;
            }
        }

    }

    public CosmoDB(CosmosClient client) {
        CosmosDBSource.client = client;
    }

    private CosmosContainer container;

    public void close() {
        client.close();
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

    public <T> List<T> sql(Class<T> clazz, String fmt, Object ... args) {
        return tryCatch(() -> {
            var res = container.queryItems(String.format(fmt, args), new CosmosQueryRequestOptions(), clazz);
            return res.stream().toList();
        }).value();
    }

    public <T> Result<T> getOne(String id, Class<T> clazz) {
        return tryCatch( () -> container.readItem(id, partitionKey, clazz).getItem());
    }

    public <T> Result<T> getOne(String id, Class<T> clazz, Transaction<CosmosBatch> trans) {
        return trans.add(batch -> batch.readItemOperation(id).getItem());
    }

    public <T> Result<T> deleteOne(T obj) {
        return tryCatch( () -> (T) container.deleteItem( obj, new CosmosItemRequestOptions()).getItem());
    }

    public <T> Result<T> deleteOne(String id, T obj, Transaction<CosmosBatch> trans) {
        return trans.add( batch -> batch.deleteItemOperation(id).getItem() );
    }

    public <T> Result<T> updateOne(T obj) {
        return tryCatch( () -> container.upsertItem(obj).getItem());
    }

    public <T> Result<T> updateOne(T obj, Transaction<CosmosBatch> trans) {
        return trans.add( batch -> batch.upsertItemOperation(obj).getItem());
    }

    public <T> Result<T> insertOne( T obj) {
        return tryCatch( () -> container.createItem(obj).getItem());
    }

    public <T> Result<T> insertOne(T obj, Transaction<CosmosBatch> trans) {
        return trans.add( batch -> batch.createItemOperation(obj).getItem());
    }


    public <T> Result<T> transaction( Consumer<Transaction<CosmosBatch>> c) {

        CosmosBatch batch = CosmosBatch.createCosmosBatch(partitionKey);

        c.accept(new CosmoDBTrans(batch));

        CosmosBatchResponse response = container.executeCosmosBatch(batch);

        if (response.isSuccessStatusCode()) {
            return Result.ok();
        } else {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    public <T> Result<T> transaction( Function<CosmosBatch, Result<T>> func) {
        CosmosBatch batch = CosmosBatch.createCosmosBatch(partitionKey);
        func.apply(batch);

        CosmosBatchResponse response = container.executeCosmosBatch(batch);

        if (response.isSuccessStatusCode()) {
            return Result.ok();
        } else {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

      <T> Result<T> tryCatch(Supplier<T> supplierFunc) {
        try {
            init();
            return Result.ok(supplierFunc.get());
        } catch( CosmosException ce ) {
            //ce.printStackTrace();
            return Result.error ( errorCodeFromStatus(ce.getStatusCode() ));
        } catch( Exception x ) {
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
