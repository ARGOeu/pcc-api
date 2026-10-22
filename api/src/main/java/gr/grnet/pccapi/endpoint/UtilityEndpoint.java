package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.validation.ValidateUrlRequestDto;
import gr.grnet.pccapi.dto.validation.ValidateUrlResponseDto;
import gr.grnet.pccapi.service.ValidationService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Utilities")
@Path("/")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UtilityEndpoint {

    @Inject
    ValidationService validationService;

    @Operation(summary = "Validate URL")
    @APIResponse(
            responseCode = "200",
            description = "URL validation result",
            content = @Content(schema = @Schema(
                    implementation = ValidateUrlResponseDto.class)))
    @POST
    @Path("/validate-url")
    public Response validateUrl(
            @Valid ValidateUrlRequestDto request) {

        return Response.ok(validationService.validateUrl(request.url)).build();
    }
}