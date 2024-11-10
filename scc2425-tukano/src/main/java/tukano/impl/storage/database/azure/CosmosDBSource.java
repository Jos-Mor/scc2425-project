package main.java.tukano.impl.storage.database.azure;

import com.azure.cosmos.*;
import com.azure.cosmos.models.PartitionKey;

import static main.java.tukano.impl.rest.TukanoRestServer.Log;

public class CosmosDBSource {


    private static final String CONNECTION_URL = System.getenv("COSMOSDB_URL");
    private static final String DB_KEY = System.getenv("COSMOSDB_KEY");
    private static final String DB_NAME = System.getenv("COSMOSDB_DATABASE");

    protected static synchronized void init() {
        if (short_container != null) return;
        Log.info("conn url: " +CONNECTION_URL + " / db key: " + DB_KEY  + " / db name: " + DB_NAME);

        client = new CosmosClientBuilder()
                .endpoint(CONNECTION_URL)
                .key(DB_KEY)
                //.directMode()
                .gatewayMode()
                // replace by .directMode() for better performance
                .consistencyLevel(ConsistencyLevel.SESSION)
                .connectionSharingAcrossClientsEnabled(true)
                .contentResponseOnWriteEnabled(true)
                .buildClient();
        Log.info("client: " + client);

        db = client.getDatabase(DB_NAME);
        Log.info("db: " + db);
        user_container = db.getContainer(USERS_CONTAINER);
        Log.info("user_container: " + user_container);
        short_container = db.getContainer(SHORTS_CONTAINER);
        Log.info("short_container: " + short_container);

    }

    protected static final String USERS_CONTAINER = "users";

    protected static final String SHORTS_CONTAINER = "shorts";

    protected static CosmosClient client;
    private static CosmosDatabase db;
    protected static CosmosContainer user_container;

    protected static CosmosContainer short_container;


}
