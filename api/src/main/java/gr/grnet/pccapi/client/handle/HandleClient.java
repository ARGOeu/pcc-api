package gr.grnet.pccapi.client.handle;

import io.quarkus.rest.client.reactive.Url;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface HandleClient {


    /**
     * Creates a Handle under the specified Prefix.
     *
     * @param prefix  the Prefix of the Handle
     * @param suffix  the suffix of the Handle
     * @param request the request sent to the Handle service
     */
    @PUT
    @Path("/api/handles/{prefix}/{suffix}")
    HandleClientResponse create(
            @HeaderParam("Authorization") String authorization,
            @PathParam("prefix") String prefix,
            @PathParam("suffix") String suffix,
            HandleClientRequest request);

    @GET
    @Path("/api/handles")
    HandleClientListResponse getAll(
            @HeaderParam("Authorization") String authorization,
            @QueryParam("prefix") String prefix);
}