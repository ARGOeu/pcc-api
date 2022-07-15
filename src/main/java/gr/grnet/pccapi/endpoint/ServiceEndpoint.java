package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.service.ServiceService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controller that provides all necessary service related functionality
 */
@Path("/services")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceEndpoint {

    @Inject
    ServiceService service;

    /**
     *
     * List all available services
     */
    @GET
    public Response listAllServices(){
        return Response.ok().entity(service.findAllServices()).build();
    }
}