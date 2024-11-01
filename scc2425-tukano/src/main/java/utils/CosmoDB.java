package main.java.utils;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.*;
import main.java.tukano.api.Result;
import org.hibernate.Session;

public class CosmoDB {
    private final String CONNECTION_URL = "https://scc2425259457.documents.azure.com:443/"; // replace with your own
    private final String DB_KEY = System.getenv("DB_KEY");
    private final String DB_NAME = "scc2425";
    private final String USERS_CONTAINER = "users";

    private CosmoDB instance;

    public synchronized CosmoDB getInstance() {
        if( instance != null)
            return instance;

        CosmosClient client = new CosmosClientBuilder()
                .endpoint(CONNECTION_URL)
                .key(DB_KEY)
                //.directMode()
                .gatewayMode()
                // replace by .directMode() for better performance
                .consistencyLevel(ConsistencyLevel.SESSION)
                .connectionSharingAcrossClientsEnabled(true)
                .contentResponseOnWriteEnabled(true)
                .buildClient();
        instance = new CosmoDB( client);
        return instance;
    }

    private CosmosClient client;
    private CosmosDatabase db;
    private CosmosContainer user_container;

    public CosmoDB(CosmosClient client) {
        this.client = client;
    }

    private   synchronized void init() {
        if( db != null)
            return;
        db = client.getDatabase(DB_NAME);
        user_container = db.getContainer(USERS_CONTAINER);


    }

    public void close() {
        client.close();
    }

    public <T> List<T> sql(String query, Class<T> clazz) {
        return tryCatch(() -> {
            var res = user_container.queryItems(query, new CosmosQueryRequestOptions(), clazz);
            return res.stream().toList();
        }).value();
    }

    public <T> List<T> sql(Class<T> clazz, String fmt, Object ... args) {
        return tryCatch(() -> {
            var res = user_container.queryItems(String.format(fmt, args), new CosmosQueryRequestOptions(), clazz);
            return res.stream().toList();
        }).value();
    }

    public <T> Result<T> getOne(String id, Class<T> clazz) {
        return tryCatch( () -> user_container.readItem(id, new PartitionKey(id), clazz).getItem());
    }

    public <T> Result<T> deleteOne(T obj) {
        return tryCatch( () -> (T) user_container.deleteItem( obj, new CosmosItemRequestOptions()).getItem()); //TODO: Fix this
    }

    public <T> Result<T> updateOne(T obj) {
        return tryCatch( () -> user_container.upsertItem(obj).getItem());
    }

    public <T> Result<T> insertOne( T obj) {
        return tryCatch( () -> user_container.createItem(obj).getItem());
    }


    public <T> Result<T> transaction( PartitionKey key, Consumer<CosmosBatch> c) {
        CosmosBatch batch = CosmosBatch.createCosmosBatch(key);
        c.accept(batch);
        CosmosBatchResponse response =  user_container.executeCosmosBatch(batch);
        if(response.isSuccessStatusCode()) {
            return Result.ok();
        }
        else {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    public <T> Result<T> transaction( Function<Session, Result<T>> func) {
        return Hibernate.getInstance().execute( func );
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
