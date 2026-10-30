package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.dto.PrefixRequestInitialDto;
import gr.grnet.pccapi.dto.PrefixRequestResponse;
import gr.grnet.pccapi.dto.pagination.PageResource;
import gr.grnet.pccapi.dto.prefix.PrefixResponseDto;
import gr.grnet.pccapi.service.PrefixRequestService;
import gr.grnet.pccapi.service.Utility;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.Getter;
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
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;

import java.util.List;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Tag(name = "Prefix Request", description = "PrefixRequest is an request that inquire the creation of the prefix")
@Path("/prefix-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityScheme(
        securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)

public class PrefixRequestEndpoint {

    @Inject
    PrefixRequestService prefixRequestService;

    @Inject
    Utility utility;

    @Operation(summary = "Get all prefix requests by creator")
    @APIResponse(
            responseCode = "200",
            description = "Prefix Requests by creator retrieved.",
            content =
            @Content(
                    schema =
                    @Schema(type = SchemaType.ARRAY, implementation = PageableObjects.class)))
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
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = APIResponseMsg.class)))

    @GET
    @SecuredEndpoint
    public Response getAll(@Parameter(name = "search", in = QUERY,
                                       description = "Search prefix requests by text on Contact Name, Surname, Contact Email, Contact Phone, Project Name, Project Description,Provider Name.",
                                       example = "GRNET")
                               @QueryParam("search")
                               String search,
                           @Parameter(name = "page", in = QUERY,
                                   description = "Page number. Must be >= 1.")
                               @DefaultValue("1")
                               @Min(value = 1, message = "Page number must be >= 1.")
                               @QueryParam("page")
                               int page,
                           @Parameter(name = "size", in = QUERY,
                                   description = "Page size.")
                               @DefaultValue("10")
                               @Min(value = 1, message = "Page size must be between 1 and 100.")
                               @Max(value = 100, message = "Page size must be between 1 and 100.")
                               @QueryParam("size")
                               int size,
                           @Context UriInfo uriInfo) {

        return Response.ok(prefixRequestService.fetchAll(utility.getUserUniqueIdentifier(),search,page-1,size, uriInfo)).build();
    }

    @Operation(summary = "Get prefix request by id")
    @APIResponse(
            responseCode = "200",
            description = "Prefix Request retrieved.",
            content =
            @Content(
                    schema =
                    @Schema(type = SchemaType.OBJECT, implementation = PrefixRequestResponse.class)))
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
            responseCode = "404",
            description = "PrefixRequest not found.",
            content =
            @Content(
                    schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content =
            @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = APIResponseMsg.class)))
    @GET
    @Path("/{id}")
    @SecuredEndpoint
    public Response getById(@PathParam("id") int id) {

        return Response.ok(prefixRequestService.fetchById(id, utility.getUid())).build();
    }


    @Operation(summary = "Get all assigned prefix requests to provider")
    @APIResponse(
            responseCode = "200",
            description = "Prefix Requests assigned to a specific provider retrieved.",
            content =
            @Content(
                    schema =
                    @Schema(type = SchemaType.ARRAY, implementation = PageableObjects.class)))
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
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = APIResponseMsg.class)))

    @GET
    @Path("/by-provider")
    @SecuredEndpoint
    public Response getAllAssigned(@Parameter(name = "search", in = QUERY,
                                               description = "Search prefix requests by text on Contact Name, Surname, Contact Email, Contact Phone, Project Name, Project Description,Provider Name.",
                                               example = "GRNET")
                                       @QueryParam("search")
                                       String search,
                                   @Parameter(name = "page", in = QUERY,
                                           description = "Page number. Must be >= 1.")
                                   @DefaultValue("1")
                                   @Min(value = 1, message = "Page number must be >= 1.")
                                   @QueryParam("page")
                                   int page,
                                   @Parameter(name = "size", in = QUERY,
                                           description = "Page size.")
                                   @DefaultValue("10")
                                   @Min(value = 1, message = "Page size must be between 1 and 100.")
                                   @Max(value = 100, message = "Page size must be between 1 and 100.")
                                   @QueryParam("size")
                                   int size,
                                   @Context UriInfo uriInfo

    ) {

        return Response.ok(prefixRequestService.fetchAssigned(search, page-1, size,uriInfo)).build();
    }

    @Operation(summary = "Create Prefix Request")
    @APIResponse(
            responseCode = "201",
            description = "Prefix Request created",
            content = @Content(schema = @Schema(
                    implementation = PrefixRequestResponse.class)))
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
            description = "Prefix request not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "Prefix already exists",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @POST
    @SecuredEndpoint
    public Response create(@Valid PrefixRequestInitialDto prefixRequestInitialDto) {

        var response = prefixRequestService.create(prefixRequestInitialDto, utility.getUserUniqueIdentifier());

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @Operation(summary = "Update Prefix Request")
    @APIResponse(
            responseCode = "201",
            description = "Prefix Request updated",
            content = @Content(schema = @Schema(
                    implementation = PrefixRequestResponse.class)))
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
            description = "Prefix request not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "Prefix request already exists",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @PATCH
    @Path("/{id}")
    @SecuredEndpoint()
    public Response update(@PathParam("id") int id, @Valid PrefixRequestInitialDto prefixRequestInitialDto) {

        var response = prefixRequestService.update(id, prefixRequestInitialDto, utility.getUserUniqueIdentifier());

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @Operation(summary = "Delete prefix request")
    @APIResponse(
            responseCode = "200",
            description = "Prefix request deleted",
            content = @Content(schema = @Schema(
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
            description = "Prefix not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @DELETE
    @Path("/{id}")
    @SecuredEndpoint()
    public Response deleteById(@PathParam("id") Integer id) {

        prefixRequestService.deleteById(id, utility.getUserUniqueIdentifier());

        var informativeResponse = new InformativeResponse();
        informativeResponse.code = 200;
        informativeResponse.message = "Prefix has been successfully deleted.";

        return Response.ok().entity(informativeResponse).build();
    }

    @Operation(summary = "Get prefix request by id assigned to provider")
    @APIResponse(
            responseCode = "200",
            description = "Prefix Request assigned to provider retrieved.",
            content =
            @Content(
                    schema =
                    @Schema(type = SchemaType.OBJECT, implementation = PrefixRequestResponse.class)))
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
            responseCode = "404",
            description = "PrefixRequest not found.",
            content =
            @Content(
                    schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content =
            @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = APIResponseMsg.class)))
    @GET
    @Path("/{id}/by-provider")
    @SecuredEndpoint
    public Response assignedById(@PathParam("id") int id) {

        return Response.ok(prefixRequestService.fetchAssignedById(id)).build();
    }
    public static class PageableObjects extends PageResource<PrefixRequestResponse> {

        private List<PrefixRequestResponse> content;

        @Override
        public List<PrefixRequestResponse> getContent() {
            return content;
        }

        @Override
        public void setContent(List<PrefixRequestResponse> content) {
            this.content = content;
        }
    }
}
