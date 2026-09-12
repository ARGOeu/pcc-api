package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.dto.handle.HandleListResponseDto;
import gr.grnet.pccapi.dto.handle.HandleRequestDto;
import gr.grnet.pccapi.dto.handle.HandleResponseDto;
import gr.grnet.pccapi.service.HandleService;
import gr.grnet.pccapi.service.Utility;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.hibernate.validator.constraints.URL;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Tag(name = "Handle")
@Path("/prefixes/{id}/handles")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@SecurityScheme(
        securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
public class HandleEndpoint {

    @Inject
    HandleService handleService;

    @Inject
    Utility utility;

    @POST
    @Operation(summary = "Create handle")
    @APIResponse(
            responseCode = "201",
            description = "Handle created",
            content = @Content(schema = @Schema(
                    implementation = HandleResponseDto.class)))
    @APIResponse(
            responseCode = "400",
            description = "Invalid request",
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
            description = "Prefix not found",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "Handle already exists",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "502",
            description = "PID provider communication error",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    public Response create(
            @Parameter(
                    name = "id",
                    description = "The identifier of the Prefix.",
                    example = "2")
            @PathParam("id")
            Integer prefixId,
            @Parameter(
                    name = "x-handle-service-url",
                    description = "The URL of the Handle service.",
                    example = "https://hdl.grnet.gr:8001/",
                    required = true)
            @HeaderParam("x-handle-service-url")
            @URL(
                    protocol = "https",
                    message = "Handle service URL must be a valid HTTPS URL.")
            String serviceUrl,
            @Parameter(
                    name = "x-handle-username",
                    description = "The Handle administrator username.",
                    example = "TESTUSER08",
                    required = true)
            @HeaderParam("x-handle-username")
            String handleUsername,
            @Parameter(
                    name = "x-handle-token",
                    description = "The token used to authenticate against the Handle service.",
                    required = true)
            @HeaderParam("x-handle-token")
            String token,
            @Valid HandleRequestDto request) {

        var handle = handleService.create(prefixId, serviceUrl, handleUsername, token, request);

        return Response.status(Response.Status.CREATED).entity(handle).build();
    }

    @GET
    @Operation(summary = "Retrieve handles")
    @APIResponse(
            responseCode = "200",
            description = "Handles retrieved",
            content = @Content(schema = @Schema(
                    implementation = HandleListResponseDto.class)))
    @APIResponse(
            responseCode = "400",
            description = "Invalid request",
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
            description = "Prefix not found",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "502",
            description = "PID provider communication error",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    public Response getAll(
            @Parameter(
                    name = "id",
                    description = "The identifier of the Prefix.",
                    example = "2")
            @PathParam("id")
            Integer prefixId,
            @Parameter(
                    name = "x-handle-service-url",
                    description = "The URL of the Handle service.",
                    example = "https://hdl.grnet.gr:8001/",
                    required = true)
            @HeaderParam("x-handle-service-url")
            @URL(
                    protocol = "https",
                    message = "Handle service URL must be a valid HTTPS URL.")
            String serviceUrl,
            @Parameter(
                    name = "x-handle-username",
                    description = "The Handle administrator username.",
                    example = "TESTUSER08",
                    required = true)
            @HeaderParam("x-handle-username")
            String handleUsername,
            @Parameter(
                    name = "x-handle-token",
                    description = "The token used to authenticate against the Handle service.",
                    required = true)
            @HeaderParam("x-handle-token")
            String token,
            @Parameter(
                    name = "search",
                    in = QUERY,
                    description = "Filters Handles by identifier.")
            @QueryParam("search")
            String search,
            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.")
            @DefaultValue("1")
            @Min(value = 1, message = "Page number must be >= 1.")
            @QueryParam("page") int page,
            @Parameter(name = "size", in = QUERY,
                    description = "The page size.")
            @DefaultValue("10")
            @Min(value = 1, message = "Page size must be between 1 and 100.")
            @Max(value = 100, message = "Page size must be between 1 and 100.")
            @QueryParam("size") int size,
            @Context UriInfo uriInfo) {

        var handles = handleService.fetchAllHandlesByPrefixId(prefixId, serviceUrl, handleUsername, token, search, page, size, uriInfo);

        return Response.ok(handles).build();
    }
}