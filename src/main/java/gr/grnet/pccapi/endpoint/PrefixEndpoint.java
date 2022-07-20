package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.service.PrefixService;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/prefixes")
@AllArgsConstructor
public class PrefixEndpoint {

    PrefixService prefixService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PrefixDto create(@RequestBody PrefixDto prefixDto) {
        return prefixService.create(prefixDto);
    }

}
