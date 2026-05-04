package gr.grnet.pccapi.client.eoscportal;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/vocabulary")
@RegisterRestClient
public interface EOSCPortalClient {

  @GET
  @Path("/byType/{type}")
  CompletionStage<Set<EOSCPortalDomain>> getByType(@PathParam("type") String type);
}
