package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.service.DomainService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/domains")
public class DomainEndpoint {

    DomainService domainService;

    public DomainEndpoint(DomainService domainService) {
        this.domainService = domainService;
    }

    @GET
    public Response getAll(){

        var domains = domainService.getAll();

        return Response.ok().entity(domains).build();
    }
}