package scc.serverless;

import com.microsoft.azure.functions.annotation.*;

import scc.data.User;
import scc.utils.GSON;

import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.microsoft.azure.functions.*;

public class CosmosDBFunction {
	private static final String COSMOSDB_FUNCTION_NAME="CosmosDBTest";

	private static final String COSMOSDB_TRIGGER_NAME="CosmosDBTriggerTest";
	
	private static final String COSMOSDB_DATABASE_NAME="cosmosdb2163";
	private static final String COSMOSDB_CONTAINER_NAME="users";
	private static final String COSMOSDB_CONNECTION_ENV="AzureCosmosDBConnection";
	private static final String COSMOSDB_LEASES_CONTAINER="leases";

		
    @FunctionName(COSMOSDB_FUNCTION_NAME)
    public void run(@CosmosDBTrigger(name = COSMOSDB_TRIGGER_NAME,
    								 databaseName = COSMOSDB_DATABASE_NAME,
    										connection = COSMOSDB_CONNECTION_ENV, 
    										containerName = COSMOSDB_CONTAINER_NAME,
    										leaseContainerName=COSMOSDB_LEASES_CONTAINER,
    										createLeaseContainerIfNotExists = true)
        					 String usersJson,
        					 final ExecutionContext context ) {
    	
    	var list = GSON.decode(usersJson, new TypeToken<List<User>>() {});
    	for( var u : list )
    		context.getLogger().info("Added user: " + u);
    }
}
