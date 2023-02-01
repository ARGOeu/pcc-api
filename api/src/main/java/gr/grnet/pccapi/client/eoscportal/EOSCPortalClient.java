package gr.grnet.pccapi.client.eoscportal;

import java.util.Set;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/vocabulary")
@RegisterRestClient
public interface EOSCPortalClient {

  @GET
  @Path("/byType/{type}")
  CompletionStage<Set<EOSCPortalDomain>> getByType(@PathParam("type") String type);
}
