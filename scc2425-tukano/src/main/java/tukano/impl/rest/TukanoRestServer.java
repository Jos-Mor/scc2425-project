package main.java.tukano.impl.rest;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import main.java.tukano.impl.Token;
import main.java.tukano.impl.rest.utils.Props;
import main.java.utils.IP;
import jakarta.ws.rs.core.Application;

//TODO: fix this code, added a few bits that needed to be added, but professor changed the code too fast to copy it all :/
//Also, remember to put TukanoRestServer on the pom.xml file


public class TukanoRestServer extends Application {
	public static final Logger Log = Logger.getLogger(TukanoRestServer.class.getName());

	static final String INETADDR_ANY = "0.0.0.0";
	static String SERVER_BASE_URI = "http://%s:%s/rest";

	public static final int PORT = 8080;

	public static String serverURI;
	private Set<Class<?>> resources = new HashSet<>();
	private Set<Object> singletons = new HashSet<>();


	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}

	public TukanoRestServer() {
		serverURI = String.format(SERVER_BASE_URI, IP.hostname(), PORT);

		Log.info("init server at " + serverURI);
		//professor's code
		resources.add(RestBlobsResource.class);
		resources.add(RestUsersResource.class);
		resources.add(RestShortsResource.class);
        resources.add(ControlResource.class);

		Log.info("added auth");

		//singletons.add(AuthenticationCookie.class);

		Props.load("azurekeys-northeurope.props"); //place the props file in resources folder under java/main

		Token.setSecret( System.getenv("TOKEN_SECRET"));

		Log.info("finished server init");
	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}

	/*
	protected void start() throws Exception {

		ResourceConfig config = new ResourceConfig();

		config.register(RestBlobsResource.class);
		config.register(RestUsersResource.class);
		config.register(RestShortsResource.class);
		config.register(ControlResource.class);


		JdkHttpServerFactory.createHttpServer( URI.create(serverURI.replace(IP.hostname(), INETADDR_ANY)), config);

		Log.info(String.format("Tukano Server ready @ %s\n",  serverURI));
	}




	public static void main(String[] args) throws Exception {
		Args.use(args);

		Token.setSecret( Args.valueOf("-secret", ""));
//		Props.load( Args.valueOf("-props", "").split(","));

		new TukanoRestServer().start();
	}

	*/
}
