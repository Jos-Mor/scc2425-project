package scc.mgt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.UniqueKey;
import com.azure.cosmos.models.UniqueKeyPolicy;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount.DefinitionStages.WithConsistencyPolicy;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount.DefinitionStages.WithCreate;
import com.azure.resourcemanager.cosmos.models.KeyKind;
import com.azure.resourcemanager.redis.models.RedisAccessKeys;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisKeyType;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.models.BlobContainer;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;

public class AzureManagement {
	// TODO: These variable allow you to control what is being created
	static final boolean CREATE_REDIS = false;
	static final boolean CREATE_STORAGE = true;
	static final boolean CREATE_COSMOSDB = true;
	static final boolean CREATE_FUNCTIONS = true;

	// TODO: change your suffix and other names if you want
	static final String MY_ID = "xxxxx"; // Add your suffix here
	
	static final String AZURE_COSMOSDB_NAME = "cosmos" + MY_ID;	// Cosmos DB account name
	static final String AZURE_COSMOSDB_DATABASE = "cosmosdb" + MY_ID;	// Cosmos DB database name
	static final String[] BLOB_CONTAINERS = { "shorts" };	// TODO: Containers to add to the blob storage

	static final Region[] REGIONS = new Region[] { Region.EUROPE_NORTH}; // Define the regions to deploy resources here
	
	// Name of resource group for each region
	static final String[] AZURE_RG_REGIONS = Arrays.stream(REGIONS)
			.map(reg -> "rg" + MY_ID + "-" + reg.name() ).toArray(String[]::new);

	// Name of application server to be launched in each regions -- launching the application
	// server must be done using mvn, as you have been doing
	// TODO: this name should be the same as defined in your app
	static final String[] AZURE_APP_NAME = Arrays.stream(REGIONS).map(reg -> "app" + MY_ID + reg.name())
			.toArray(String[]::new);

	// Name of Blob storage account
	static final String[] AZURE_STORAGE_NAME = Arrays.stream(REGIONS).map(reg -> "sto" + MY_ID + reg.name())
			.toArray(String[]::new);
	
	// Name of Redis server to be defined
	static final String[] AZURE_REDIS_NAME = Arrays.stream(REGIONS).map(reg -> "redis" + MY_ID + reg.name())
			.toArray(String[]::new);
		
	// Name of Azure functions to be launched in each regions
	static final String[] AZURE_FUNCTIONS_NAME = Arrays.stream(REGIONS).map(reg -> "fun" + MY_ID + reg.name())
			.toArray(String[]::new);

	// Name of property file with keys and URLS to access resources
	static final String[] AZURE_PROPS_LOCATIONS = Arrays.stream(REGIONS)
			.map(reg -> "azurekeys-" + reg.name() + ".props").toArray(String[]::new);
	
	// Name of shell script file with commands to set application setting for you application server
	// and Azure functions
	static final String[] AZURE_SETTINGS_LOCATIONS = Arrays.stream(REGIONS)
			.map(reg -> "azureprops-" + reg.name() + ".sh").toArray(String[]::new);
		
	public static AzureResourceManager createManagementClient() throws IOException {
		AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
		TokenCredential credential = new DefaultAzureCredentialBuilder()
		    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
		    .build();
		AzureResourceManager azure = AzureResourceManager
		    .authenticate(credential, profile)
		    .withDefaultSubscription();
		System.out.println("Azure client created with success");
		return azure;
	}

	public static ResourceGroup createResourceGroup(AzureResourceManager azure, String rgName, Region region) {
		ResourceGroup resourceGroup = azure.resourceGroups().define(rgName).withRegion(region).create();
		return resourceGroup;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Azure Storage Account CODE
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static StorageAccount createStorageAccount(AzureResourceManager azure, String rgName, String name, Region region) {
		System.out.println("Creating Storage account: name = " + name + " ; group = " + rgName
				+ " ; region = " + region.name());
		StorageAccount storageAccount = azure.storageAccounts().define(name).withRegion(region)
				.withNewResourceGroup(rgName).withGeneralPurposeAccountKindV2()
				.withAccessFromAllNetworks()
				.withSku(StorageAccountSkuType.STANDARD_LRS)
				.create();
		storageAccount.innerModel().allowBlobPublicAccess();
		System.out.println("Storage account created with success: name = " + name + " ; group = " + rgName
				+ " ; region = " + region.name());
		return storageAccount;
	}

	private static BlobContainer createBlobContainer(AzureResourceManager azure, String rgName, String accountName,
			String containerName) {
		BlobContainer container = azure.storageBlobContainers().defineContainer(containerName)
				.withExistingStorageAccount(rgName, accountName).withPublicAccess(PublicAccess.NONE).create();
		System.out.println("Blob container created with success: name = " + containerName + " ; group = " + rgName
				+ " ; account = " + accountName);
		return container;
	}

	public synchronized static void recordStorageKey(AzureResourceManager azure, String propFilename, String settingsFilename,
			String functionsName, String functionsRGName, StorageAccount account) throws IOException {
	}

	public synchronized static void dumpStorageKey(Map<String, String> props, String propFilename,
			String settingsFilename, String appName, String functionName, String rgName, StorageAccount account)
			throws IOException {
		List<StorageAccountKey> storageAccountKeys = account.getKeys();
		storageAccountKeys = account.regenerateKey(storageAccountKeys.get(0).keyName());

		StringBuffer keyB = new StringBuffer();
		keyB.append("DefaultEndpointsProtocol=https;AccountName=");
		keyB.append(account.name());
		keyB.append(";AccountKey=");
		keyB.append(storageAccountKeys.get(0).value());
		keyB.append(";EndpointSuffix=core.windows.net");
		String key = keyB.toString();

		synchronized (props) {
			props.put("BlobStoreConnection", key);
		}

		synchronized (AzureManagement.class) {
			Files.write(Paths.get(propFilename), ("BlobStoreConnection=" + key + "\n").getBytes(),
					StandardOpenOption.APPEND);
		}
		StringBuffer cmd = new StringBuffer();
		if (functionName != null) {
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(functionName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"BlobStoreConnection=");
			cmd.append(key);
			cmd.append("\"\n");
		}
		if (appName != null) {
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(appName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"BlobStoreConnection=");
			cmd.append(key);
			cmd.append("\"\n");
		}
		synchronized (AzureManagement.class) {
			Files.write(Paths.get(settingsFilename), cmd.toString().getBytes(), StandardOpenOption.APPEND);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// COSMOS DB CODE
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static CosmosDBAccount createCosmosDBAccount(AzureResourceManager azure, String rgName, String name, Region[] regions) {
		System.out.println("Creating CosmosDB account: name = " + name + " ; group = " + rgName
				+ " ; main region = " + regions[0].name() + " ; number regions = " + regions.length);

		WithConsistencyPolicy step = azure.cosmosDBAccounts().define(name).withRegion(regions[0])
				.withExistingResourceGroup(rgName).withDataModelSql();
		CosmosDBAccount account = null;
		if (regions.length == 1) {
			account = step.withSessionConsistency().withWriteReplication(regions[0]).create();
		} else {
			WithCreate create = step.withSessionConsistency().withWriteReplication(regions[0])
					.withMultipleWriteLocationsEnabled(true);
			for (int i = 1; i < regions.length; i++) {
				create = create.withSessionConsistency().withWriteReplication(regions[i]);
			}
			account = create.create();
		}
		account.regenerateKey(KeyKind.PRIMARY);
		System.out.println("CosmosDB account created with success: name = " + name + " ; group = " + rgName
				+ " ; main region = " + regions[0].name() + " ; number regions = " + regions.length);
		return account;
	}

	public synchronized static void dumpCosmosDBKey(Map<String, String> props, String propFilename,
			String settingsFilename, String appName, String functionName, String rgName, String databaseName,
			CosmosDBAccount account) throws IOException {
		synchronized (AzureManagement.class) {
			Files.write(Paths.get(propFilename),
					("COSMOSDB_KEY=" + account.listKeys().primaryMasterKey() + "\n").getBytes(),
					StandardOpenOption.APPEND);
			Files.write(Paths.get(propFilename), ("COSMOSDB_URL=" + account.documentEndpoint() + "\n").getBytes(),
					StandardOpenOption.APPEND);
			Files.write(Paths.get(propFilename), ("COSMOSDB_DATABASE=" + databaseName + "\n").getBytes(),
					StandardOpenOption.APPEND);
		}
		synchronized (props) {
			props.put("COSMOSDB_KEY", account.listKeys().primaryMasterKey());
			props.put("COSMOSDB_URL", account.documentEndpoint());
			props.put("COSMOSDB_DATABASE", databaseName);
		}

		StringBuffer cmd = new StringBuffer();
		if (appName != null) {
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(appName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"COSMOSDB_KEY=");
			cmd.append(account.listKeys().primaryMasterKey());
			cmd.append("\"\n");
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(appName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"COSMOSDB_URL=");
			cmd.append(account.documentEndpoint());
			cmd.append("\"\n");
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(appName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"COSMOSDB_DATABASE=");
			cmd.append(databaseName);
			cmd.append("\"\n");
		}
		if (functionName != null) {
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(functionName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"AzureCosmosDBConnection=AccountEndpoint=");
			cmd.append(account.documentEndpoint());
			cmd.append(";AccountKey=");
			cmd.append(account.listKeys().primaryMasterKey());
			cmd.append(";\"");
			cmd.append("\n");
		}
		synchronized (AzureManagement.class) {
			Files.write(Paths.get(settingsFilename), cmd.toString().getBytes(), StandardOpenOption.APPEND);
		}
	}

	public static CosmosClient getCosmosClient(CosmosDBAccount account) {
		CosmosClient client = new CosmosClientBuilder().endpoint(account.documentEndpoint())
				.key(account.listKeys().primaryMasterKey()).directMode() // comment this is not to use direct mode
				.consistencyLevel(ConsistencyLevel.SESSION).connectionSharingAcrossClientsEnabled(true)
				.contentResponseOnWriteEnabled(true).buildClient();
		System.out.println("CosmosDB client created with success: name = " + account.name());
		return client;
	}

	static void createCosmosDatabase(CosmosClient client, String dbname) {
		// create database if not exists
		System.out.println("Creating CosmosDB database: name = " + dbname);
		CosmosDatabaseProperties props = new CosmosDatabaseProperties(dbname);
		ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(400);
		var res = client.createDatabase(props, throughputProperties);
		System.err.println( "----->" + res);
		System.out.println("CosmosDB database created with success: name = " + dbname);
	}

	static void createCosmosCollection(CosmosClient client, String dbname, String collectionName, String partKeys,
			String[] uniqueKeys) {
		try {
			System.out.println("Creating CosmosDB collection: name = " + collectionName + "@" + dbname);
			CosmosDatabase db = client.getDatabase(dbname);
			CosmosContainerProperties props = new CosmosContainerProperties(collectionName, partKeys);
			if (uniqueKeys != null) {
				UniqueKeyPolicy uniqueKeyDef = new UniqueKeyPolicy();
				List<UniqueKey> uniqueKeyL = new ArrayList<UniqueKey>();
				for (String k : uniqueKeys) {
					uniqueKeyL.add(new UniqueKey(Arrays.asList(k)));
				}
				uniqueKeyDef.setUniqueKeys(uniqueKeyL);
				props.setUniqueKeyPolicy(uniqueKeyDef);
			}
			db.createContainer(props);
			System.out.println("CosmosDB collection created with success: name = " + collectionName + "@" + dbname);

		} catch (Exception e) { // TODO: Something has gone terribly wrong.
			e.printStackTrace();
			return;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// REDIS CODE
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	public static RedisCache createRedis(AzureResourceManager azure, String rgName, String name, Region region) {
		try {
			System.out.println("Creating Redis cache : name = " + name + "@" + region);
			Creatable<RedisCache> redisCacheDefinition = azure.redisCaches().define(name).withRegion(region)
					.withNewResourceGroup(rgName).withBasicSku(0);

			return azure.redisCaches().create(redisCacheDefinition).get(redisCacheDefinition.key());
		} finally {
			System.out.println("Redis cache created with success: name = " + name + "@" + region);
		}
	}
	public synchronized static void dumpRedisCacheInfo(Map<String, String> props, String propFilename, 
				String settingsFilename, String appName, String functionName, String rgName, RedisCache cache)
			throws IOException {
		RedisAccessKeys redisAccessKey = cache.regenerateKey(RedisKeyType.PRIMARY);
		synchronized (AzureManagement.class) {
			Files.write(Paths.get(propFilename), ("REDIS_KEY=" + redisAccessKey.primaryKey() + "\n").getBytes(),
					StandardOpenOption.APPEND);
			Files.write(Paths.get(propFilename), ("REDIS_URL=" + cache.hostname() + "\n").getBytes(),
					StandardOpenOption.APPEND);
		}
		synchronized (props) {
			props.put("REDIS_KEY", redisAccessKey.primaryKey());
			props.put("REDIS_URL", cache.hostname());
		}
		StringBuffer cmd = new StringBuffer();
		if (appName != null) {
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(appName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"REDIS_KEY=");
			cmd.append(redisAccessKey.primaryKey());
			cmd.append("\"\n");
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(appName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"REDIS_URL=");
			cmd.append(cache.hostname());
			cmd.append("\"\n");
		}
		if (functionName != null) {
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(functionName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"REDIS_KEY=");
			cmd.append(redisAccessKey.primaryKey());
			cmd.append("\"\n");
			cmd.append("az functionapp config appsettings set --name ");
			cmd.append(functionName);
			cmd.append(" --resource-group ");
			cmd.append(rgName);
			cmd.append(" --settings \"REDIS_URL=");
			cmd.append(cache.hostname());
			cmd.append("\"\n");
		}
		synchronized (AzureManagement.class) {
			Files.write(Paths.get(settingsFilename), cmd.toString().getBytes(), StandardOpenOption.APPEND);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// AZURE FUNCTIONS CODE
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	public static FunctionApp createAzureFunctions(AzureResourceManager azure, String rgName, String name, Region region) {
		try {
			System.out.println("Creating Azure functions : name = " + name + "; region = " + region + " ; group = " + rgName);
			
			Creatable<FunctionApp> azureFunctionDefinition  = azure.functionApps().define(name)
					.withRegion(region)
					.withNewResourceGroup(rgName)
					.withRuntime("java")
					.withRuntimeVersion("~4");

			return azureFunctionDefinition.create();
		} finally {
			System.out.println("Created Azure functions created with success: name = " + name + "; region = " + region + "; group = " + rgName);
		}
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// AZURE DELETE CODE
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void deleteResourceGroup(AzureResourceManager azure, String rgName) {
		azure.resourceGroups().deleteByName(rgName);
	}

	public static void main(String[] args) {
		try {
			System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Error");


			final Map<String, Map<String, String>> props = new HashMap<String, Map<String, String>>();
			Arrays.stream(REGIONS).forEach(reg -> props.put(reg.name(), new HashMap<String, String>()));

			var threads = new ArrayList<Thread>();


			var azure = createManagementClient();
			if (args.length == 1 && args[0].equalsIgnoreCase("--delete")) {
				Arrays.stream(AZURE_RG_REGIONS).forEach(reg -> deleteResourceGroup(azure, reg));					
			} else {
				// Init properties files
				for (String propF : AZURE_PROPS_LOCATIONS) {
					Files.deleteIfExists(Paths.get(propF));
					Files.write(Paths.get(propF),
							("# Date : " + new SimpleDateFormat().format(new Date()) + "\n").getBytes(),
							StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				}
				// Init settings files
				for (String propF : AZURE_SETTINGS_LOCATIONS) {
					Files.deleteIfExists(Paths.get(propF));
					Files.write(Paths.get(propF), "".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				}

				// Create resource groups
				for (int i = 0; i < REGIONS.length; i++)
					createResourceGroup(azure, AZURE_RG_REGIONS[i], REGIONS[i]);

				if (CREATE_STORAGE) {
					Thread th = new Thread(() -> {
						try {
							final AzureResourceManager azure0 = createManagementClient();
							for (int i = 0; i < REGIONS.length; i++) {
								StorageAccount accountStorage = createStorageAccount(azure0, AZURE_RG_REGIONS[i],
										AZURE_STORAGE_NAME[i], REGIONS[i]);
								dumpStorageKey(props.get(REGIONS[i].name()), AZURE_PROPS_LOCATIONS[i],
										AZURE_SETTINGS_LOCATIONS[i], AZURE_APP_NAME[i], AZURE_FUNCTIONS_NAME[i],
										AZURE_RG_REGIONS[i], accountStorage);
								for (String cont : BLOB_CONTAINERS)
									createBlobContainer(azure0, AZURE_RG_REGIONS[i], AZURE_STORAGE_NAME[i], cont);
							}
							System.err.println("Azure Blobs Storage resources created with success");

						} catch (Exception e) {
							System.err.println("Error while creating storage resources");
							e.printStackTrace();
						}
						return;
					});
					threads.add(th);
					th.start();
				}

				if (CREATE_COSMOSDB) {
					Thread th = new Thread(() -> {
						try {
							final AzureResourceManager azure0 = createManagementClient();
							CosmosDBAccount accountCosmosDB = createCosmosDBAccount(azure0, AZURE_RG_REGIONS[0],
									AZURE_COSMOSDB_NAME, REGIONS);
							for (int i = 0; i < REGIONS.length; i++) {
								dumpCosmosDBKey(props.get(REGIONS[i].name()), AZURE_PROPS_LOCATIONS[i],
										AZURE_SETTINGS_LOCATIONS[i], AZURE_APP_NAME[i], AZURE_FUNCTIONS_NAME[i],
										AZURE_RG_REGIONS[i], AZURE_COSMOSDB_DATABASE, accountCosmosDB);
							}
							CosmosClient cosmosClient = getCosmosClient(accountCosmosDB);
							createCosmosDatabase(cosmosClient, AZURE_COSMOSDB_DATABASE);

							//TODO: create the collections you have in your application
							createCosmosCollection(cosmosClient, AZURE_COSMOSDB_DATABASE, "users", "/id", null);

							System.err.println("Azure Cosmos DB resources created with success");

						} catch (Exception e) {
							System.err.println("Error while creating cosmos db resources");
							e.printStackTrace();
						}
					});
					threads.add(th);
					th.start();
				}

				if (CREATE_REDIS) {
					Thread th = new Thread(() -> {
						try {
							final AzureResourceManager azure0 = createManagementClient();
							for (int i = 0; i < REGIONS.length; i++) {
								RedisCache cache = createRedis(azure0, AZURE_RG_REGIONS[i], AZURE_REDIS_NAME[i],
										REGIONS[i]);
								dumpRedisCacheInfo(props.get(REGIONS[i].name()), AZURE_PROPS_LOCATIONS[i], 
										AZURE_SETTINGS_LOCATIONS[i], AZURE_APP_NAME[i], AZURE_FUNCTIONS_NAME[i],
										AZURE_RG_REGIONS[i], cache);
							}
							System.err.println("Azure Redis resources created with success");
						} catch (Exception e) {
							System.err.println("Error while creating redis resources");
							e.printStackTrace();
						}
					});
					threads.add(th);
					th.start();
				}
				
				if (CREATE_FUNCTIONS) {
					Thread th = new Thread(() -> {
						try {
							final AzureResourceManager azure0 = createManagementClient();
							for (int i = 0; i < REGIONS.length; i++) {
								var func = createAzureFunctions(azure0, AZURE_RG_REGIONS[i], AZURE_FUNCTIONS_NAME[i], REGIONS[i]);
							}
							System.err.println("Azure Functions resources created with success");
						} catch (Exception e) {
							System.err.println("Error while creating functions resources");
							e.printStackTrace();
						}
					});
					threads.add(th);
					th.start();
				}

			}
			for (Thread th : threads) {
				th.join();
			}
		} catch (Exception e) {
			System.err.println("Error while creating resources");
			e.printStackTrace();
		}
		System.exit(0);
	}
}
