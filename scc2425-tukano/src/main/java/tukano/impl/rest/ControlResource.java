package main.java.tukano.impl.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static main.java.tukano.impl.rest.TukanoRestServer.Log;

@Path("/ctrl")
public class ControlResource
{

    /**
     * This methods just prints a string. It may be useful to check if the current
     * version is running on Azure.
     */
    @Path("/version")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String version() {
        Log.info("ver check");
        Log.info(System.getenv("COSMOSDB_URL"));
        return "v: 0001";
    }

}
