package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.service.DomainService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/domains")
public class DomainEndpoint {

    DomainService domainService;

    public DomainEndpoint(DomainService domainService) {
        this.domainService = domainService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(){

        var domains = domainService.getAll();

        return Response.ok().entity(domains).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(Integer id){

        var domain = domainService.getById(id);

        return Response.ok().entity(domain).build();
    }
}