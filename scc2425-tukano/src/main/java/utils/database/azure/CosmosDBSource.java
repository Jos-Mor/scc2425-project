package main.java.utils.database.azure;

import com.azure.cosmos.*;

public class CosmosDBSource {


    private static final String CONNECTION_URL = "https://scc2425259457.documents.azure.com:443/"; // replace with your own
    private static final String DB_KEY = System.getenv("DB_KEY");
    private static CosmoDB instance;

    public static synchronized CosmoDB getInstance() {
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

    private static final String DB_NAME = "scc2425";
    private static final String USERS_CONTAINER = "users";

    private static final String SHORTS_CONTAINER = "shorts";

    protected static CosmosClient client;
    private static CosmosDatabase db;
    public static CosmosContainer user_container;

    public static CosmosContainer short_container;


    protected static synchronized void init() {
        if( db != null)
            return;
        db = client.getDatabase(DB_NAME);
        user_container = db.getContainer(USERS_CONTAINER);
        short_container = db.getContainer(SHORTS_CONTAINER);

    }

}
