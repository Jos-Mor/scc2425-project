package main.java.utils.database.azure;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import main.java.tukano.api.Result;
import main.java.utils.Hibernate;
import main.java.utils.database.DataBase;
import org.hibernate.Session;

public class CosmoDB extends CosmosDBSource implements DataBase {

    public CosmoDB(Container type) {
        super();
        container = enumToContainer(type);
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

    public <T> List<T> sql(Class<T> clazz, String fmt, Object ... args) {
        return tryCatch(() -> {
            var res = container.queryItems(String.format(fmt, args), new CosmosQueryRequestOptions(), clazz);
            return res.stream().toList();
        }).value();
    }

    public <T> Result<T> getOne(String id, Class<T> clazz) {
        return tryCatch( () -> container.readItem(id, new PartitionKey(id), clazz).getItem());
    }

    public <T> Result<T> deleteOne(T obj) {
        return tryCatch( () -> (T) container.deleteItem( obj, new CosmosItemRequestOptions()).getItem()); //TODO: Fix this
    }

    public <T> Result<T> updateOne(T obj) {
        return tryCatch( () -> container.upsertItem(obj).getItem());
    }

    public <T> Result<T> insertOne( T obj) {
        return tryCatch( () -> container.createItem(obj).getItem());
    }

    //TODO: Transactions???

    public <T> Result<T> transaction( Consumer<Session> c) {
        return Hibernate.getInstance().execute(c);
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
