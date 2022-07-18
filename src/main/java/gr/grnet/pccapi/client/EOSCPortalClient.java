package gr.grnet.pccapi.client;

import gr.grnet.pccapi.external.response.EOSCPortalDomain;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Set;
import java.util.concurrent.CompletionStage;

@Path("/vocabulary")
@RegisterRestClient
public interface EOSCPortalClient {

    @GET
    @Path("/byType/{type}")
    CompletionStage<Set<EOSCPortalDomain>> getByType(@PathParam("type") String type);
}