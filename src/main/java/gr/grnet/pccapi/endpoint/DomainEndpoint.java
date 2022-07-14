package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.mapper.DomainMapper;
import gr.grnet.pccapi.repository.DomainRepository;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/domains")
public class DomainEndpoint {

    @Inject
    DomainRepository domainRepository;

    @GET
    public Response getAll(){

        var domains = domainRepository.findAll().list();

        return Response.ok().entity(DomainMapper.INSTANCE.domainsToDto(domains)).build();
    }
}