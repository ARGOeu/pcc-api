package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.CodelistDto;
import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.enums.ContactType;
import gr.grnet.pccapi.service.CodelistService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.endpoint.scanner.runtime.ParamRef;
import org.grnet.endpoint.scanner.runtime.ParamType;
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;

@Tag(name = "Codelist")
@Path("/codelist")
@Authenticated
@SecurityScheme(securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CodelistEndpoint {

    @Inject
    CodelistService codelistService;

    @Operation(
            summary = "Get codelist entries",
            description =
                    "Returns all codelist entries, or only entries for the given category when provided.")
    @APIResponse(
            responseCode = "200",
            description = "Codelist entries found",
            content =
            @Content(
                    schema = @Schema(type = SchemaType.ARRAY, implementation = CodelistDto.class),
                    examples = {
                            @ExampleObject(name = "All entries"),
                            @ExampleObject(name = "Lookup service types", value = "lookup_service_type"),
                            @ExampleObject(name = "Contract types", value = "contract_type"),
                            @ExampleObject(name = "Contact types", value = "contact_type")

                    }))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = APIResponseMsg.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = APIResponseMsg.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content =
            @Content(
                    schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
    @GET
    @Path("/")
    @SecuredEndpoint
    public Response getByCategory(@QueryParam("category") String category) {

        List<CodelistDto> codes = codelistService.fetchByCategory(category);

        return Response.ok(codes, MediaType.APPLICATION_JSON).build();
    }

    @Tag(name = "Codelist")
    @Operation(
            summary = "Get list of contact types.",
            description = "This endpoint returns a list of contact types ")
    @APIResponse(
            responseCode = "200",
            description = "List of contact types existing.",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = List.class)))
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Entity Not Found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/contact-types")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    @SecuredEndpoint
    public Response getContactTypes() {

        var contactTypes = codelistService.getContactTypes();

        return Response.ok().entity(contactTypes).build();
    }


}
