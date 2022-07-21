package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.DomainDto;
import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.dto.PrefixResponseDto;
import gr.grnet.pccapi.service.PrefixService;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controller that provides all the prefix related functionality
 */
@Tag(name = "Prefix")
@Path("/prefixes")
@AllArgsConstructor
public class PrefixEndpoint {

    PrefixService prefixService;

    @Operation(
            summary = "Create a new Prefix.")
    @APIResponse(
            responseCode = "201",
            description = "The particular Prefix.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PrefixResponseDto.class)))
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
            @Parameter(
                    description = "The Prefix to be saved to the service.",
                    required = true,
                    schema = @Schema(type = SchemaType.OBJECT, implementation = PrefixDto.class))
            @RequestBody PrefixDto prefixDto) {
        return Response
                .status(Response.Status.CREATED)
                .entity(prefixService.create(prefixDto))
                .build();
    }

}
