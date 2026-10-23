package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.AssignProviderDto;
import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.dto.PrefixRequestResponse;
import gr.grnet.pccapi.dto.PrefixRequestStatusDto;
import gr.grnet.pccapi.dto.metadata.RoleAssignmentMetadataResponseDto;
import gr.grnet.pccapi.dto.metadata.RoleMetadataResponseDto;
import gr.grnet.pccapi.service.PrefixRequestService;
import gr.grnet.pccapi.service.PrefixService;
import gr.grnet.pccapi.dto.statistic.AdminTotalStatistics;
import gr.grnet.pccapi.service.RoleMetadataService;
import gr.grnet.pccapi.service.StatisticsService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Path("/admin")
@Authenticated
@SecurityScheme(
        securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
public class AdminEndpoint {

    @Inject
    RoleMetadataService roleMetadataService;

    @Inject
    PrefixService prefixService;
    @Inject

    StatisticsService statisticsService;

    @Inject
    PrefixRequestService prefixRequestService;

    @Tag(name = "Admin")
    @Operation(
            summary = "Get role metadata.",
            description = "Returns the supported metadata attributes for role creation."
    )
    @APIResponse(
            responseCode = "200",
            description = "Role metadata.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = RoleMetadataResponseDto.class)))
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
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/roles/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    @SecuredEndpoint
    public Response getRoleMetadata() {

        var response = roleMetadataService.getRoleMetadata();

        return Response.ok(response).build();
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "Get role assignment metadata.",
            description = "Returns the supported metadata attributes for resource-specific role assignments."
    )
    @APIResponse(
            responseCode = "200",
            description = "Role assignment metadata.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = RoleAssignmentMetadataResponseDto.class)))
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
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/roles/assignment/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    @SecuredEndpoint
    public Response getRoleAssignmentMetadata() {

        var response = roleMetadataService.getRoleAssignmentMetadata();

        return Response.ok(response).build();
    }


    @Tag(name = "Admin")
    @Operation(summary = "Get prefixes")
    @APIResponse(
            responseCode = "200",
            description = "Prefixes found",
            content = @Content(schema = @Schema(
                    implementation = PrefixEndpoint.PageableObjects.class)))
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
    @GET
    @Path("/prefixes")
    @SecuredEndpoint()
    public Response getAllByPageAndSize(
            @PathParam("id") int id,
            @Parameter(name = "search", in = QUERY,
                    description = "Search prefixes by text.",
                    example = "21.T")
            @QueryParam("search")
            String search,
            @Parameter(name = "provider", in = QUERY,
                    description = "Filter prefixes by provider name.",
                    example = "GRNET")
            @QueryParam("provider")
            String provider,
            @Parameter(name = "domain", in = QUERY,
                    description = "Filter prefixes by domain name.",
                    example = "Life Sciences")
            @QueryParam("domain")
            String domain,
            @Parameter(name = "contract_type", in = QUERY,
                    description = "Filter prefixes by contract type name.",
                    example = "PROJECT")
            @QueryParam("contract_type")
            String contractType,
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

        var prefixes = prefixService.fetchAllPrefixesByPageAndSize(search, provider, domain, contractType,page - 1, size, uriInfo);

        return Response.ok(prefixes).build();
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "Get  Statistics Results.",
            description = "Returns results of Statistics.")
    @APIResponse(
            responseCode = "200",
            description = "Statistics found",
            content = @Content(schema = @Schema(
                    implementation = AdminTotalStatistics.class)))

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
            responseCode = "500",
            description = "Internal Server Error.",
            content =
            @Content(
                    schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))

    @GET
    @Path("/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    @SecuredEndpoint

    public Response getStatisticsRequest() {

        var statistics = statisticsService.getStatistics();

        return Response.ok().entity(statistics).build();
    }
    @Tag(name="Admin")
    @Operation(summary = "Update prefix request's status")
    @APIResponse(
            responseCode = "200",
            description = "Prefix request status updated",
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
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @PATCH
    @Path("/prefix-request/{id}/status")
    @SecuredEndpoint
    public Response updateStatus(
            @PathParam("id") Integer id,
            @Valid PrefixRequestStatusDto dto) {

        PrefixRequestResponse response =
                prefixRequestService.updateStatus(
                        id,
                        dto
                );
        return Response.ok(response).build();
    }

    @Tag(name = "Admin")
    @Operation(summary = "Update prefix request's provider")
    @APIResponse(
            responseCode = "200",
            description = "Prefix request provider updated",
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
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @PATCH
    @Path("/prefix-requests/{id}/assign-provider")
    public Response assignProvider(
            @PathParam("id") Integer id,
            @NotNull @Valid AssignProviderDto dto) {

        PrefixRequestResponse response =
                prefixRequestService.assignProvider(
                        id, dto.getProviderId());

        return Response.ok(response).build();
    }
}
