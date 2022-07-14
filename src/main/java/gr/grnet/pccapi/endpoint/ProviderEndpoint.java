package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.service.ProviderService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/providers")
public class ProviderEndpoint {

    ProviderService providerService;

    public ProviderEndpoint(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        var providers = providerService.getAll();
        return Response.ok().entity(providers).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(int id) {
        var provider = providerService.get(id);
        return Response.ok().entity(provider).build();

    }

}