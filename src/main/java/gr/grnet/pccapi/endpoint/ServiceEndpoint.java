package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.service.ServiceService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controller that provides all necessary service related functionality
 */
@Path("/services")
public class ServiceEndpoint {

    ServiceService service;

    public ServiceEndpoint(ServiceService service) {
        this.service = service;
    }

    /**
     *
     * List all available services
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllServices(){
        return Response.ok().entity(service.findAllServices()).build();
    }

    /**
     *
     * List one specific service based on the provided id.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response listOneService(int id) {
        return Response.ok().entity(service.findOneService(id)).build();
    }
}