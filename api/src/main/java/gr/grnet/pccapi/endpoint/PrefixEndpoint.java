package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.dto.pagination.PageResource;
import gr.grnet.pccapi.dto.prefix.PartialPrefixDto;
import gr.grnet.pccapi.dto.prefix.PrefixRequestDto;
import gr.grnet.pccapi.dto.prefix.PrefixResponseDto;
import gr.grnet.pccapi.dto.statistic.StatisticsDto;
import gr.grnet.pccapi.dto.statistic.StatisticsRequestDto;
import gr.grnet.pccapi.resources.PrefixResource;
import gr.grnet.pccapi.resources.ProviderResource;
import gr.grnet.pccapi.service.PrefixService;
import gr.grnet.pccapi.service.StatisticsService;
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
import org.grnet.endpoint.scanner.runtime.ParamRef;
import org.grnet.endpoint.scanner.runtime.ParamType;
import org.grnet.endpoint.scanner.runtime.Scope;
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;

import java.util.List;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Tag(name = "Prefix")
@Path("/providers/{id}/prefixes")
@Authenticated
@SecurityScheme(
        securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PrefixEndpoint {

    @Inject
    PrefixService prefixService;

    @Inject
    StatisticsService statisticsService;

    @Operation(summary = "Create prefix")
    @APIResponse(
            responseCode = "201",
            description = "Prefix created",
            content = @Content(schema = @Schema(
                    implementation = PrefixResponseDto.class)))
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
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    )
            }
    )
    public Response create(
            @PathParam("id") int id,
            @Valid PrefixRequestDto prefixRequestDto) {

        var response = prefixService.create(prefixRequestDto);

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @Operation(summary = "Update prefix")
    @APIResponse(
            responseCode = "200",
            description = "Prefix updated",
            content = @Content(schema = @Schema(
                    implementation = PrefixRequestDto.class)))
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
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @PUT
    @Path("/{prefix-id}")
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    ),
                    @ParamRef(
                            param = "prefix-id",
                            type = ParamType.PATH,
                            referTo = PrefixResource.class
                    )
            }
    )
    public Response update(
            @PathParam("id") int id,
            @PathParam("prefix-id") int prefixId,
            @Valid PrefixRequestDto prefixRequestDto) {

        return Response.ok(prefixService.update(prefixRequestDto, prefixId)).build();
    }

    @Operation(summary = "Get prefixes")
    @APIResponse(
            responseCode = "200",
            description = "Prefixes found",
            content = @Content(schema = @Schema(
                    implementation = PageableObjects.class)))
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
    @GET
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    ),
                    @ParamRef(
                            param = "prefix-id",
                            type = ParamType.PATH,
                            referTo = PrefixResource.class
                    )
            },
            scope = {Scope.ALL, Scope.MINE
            }
    )
    public Response getAllByPageAndSize(
            @PathParam("id") int id,
            @Parameter(
                    name = "search",
                    in = QUERY,
                    description = "Search prefixes by text.",
                    example = "21.T")
            @QueryParam("search")
            String search,
            @Parameter(
                    name = "provider",
                    in = QUERY,
                    description = "Filter prefixes by provider name.",
                    example = "GRNET")
            @QueryParam("provider")
            String provider,
            @Parameter(
                    name = "domain",
                    in = QUERY,
                    description = "Filter prefixes by domain name.",
                    example = "Life Sciences")
            @QueryParam("domain")
            String domain,
            @Parameter(
                    name = "contract_type",
                    in = QUERY,
                    description = "Filter prefixes by contract type name.",
                    example = "PROJECT")
            @QueryParam("contract_type")
            String contractType,
            @Parameter(
                    name = "page",
                    in = QUERY,
                    description = "Page number. Must be >= 1.")
            @DefaultValue("1")
            @Min(value = 1, message = "Page number must be >= 1.")
            @QueryParam("page")
            int page,
            @Parameter(
                    name = "size",
                    in = QUERY,
                    description = "Page size.")
            @DefaultValue("10")
            @Min(value = 1, message = "Page size must be between 1 and 100.")
            @Max(value = 100, message = "Page size must be between 1 and 100.")
            @QueryParam("size")
            int size,
            @Context UriInfo uriInfo) {

    var prefixes = prefixService.fetchByPageAndSize(search, provider, domain, contractType,page - 1, size, uriInfo);

        return Response.ok(prefixes).build();
    }

    @Operation(summary = "Patch prefix")
    @APIResponse(
            responseCode = "200",
            description = "Prefix updated",
            content = @Content(schema = @Schema(
                    implementation = PrefixResponseDto.class)))
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
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @PATCH
    @Path("/{prefix-id}")
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    ),
                    @ParamRef(
                            param = "prefix-id",
                            type = ParamType.PATH,
                            referTo = PrefixResource.class
                    )
            }
    )
    public Response patch(
            @PathParam("id") int id,
            @PathParam("prefix-id") int prefixId,
            @Valid PartialPrefixDto prefixDto) {

        var prefix = prefixService.patchById(prefixId, prefixDto);

        return Response.ok(prefix).build();
    }

    @Operation(summary = "Delete prefix")
    @APIResponse(
            responseCode = "200",
            description = "Prefix deleted",
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
    @Path("/{prefix-id}")
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    ),
                    @ParamRef(
                            param = "prefix-id",
                            type = ParamType.PATH,
                            referTo = PrefixResource.class
                    )
            }
    )
    public Response deleteById(
            @PathParam("id") int id,
            @PathParam("prefix-id") Integer prefixId) {

        prefixService.deleteById(prefixId);

        var informativeResponse = new InformativeResponse();
        informativeResponse.code = 200;
        informativeResponse.message = "Prefix has been successfully deleted.";

        return Response.ok().entity(informativeResponse).build();
    }

    @Operation(summary = "Get prefix by id")
    @APIResponse(
            responseCode = "200",
            description = "Prefix found",
            content = @Content(schema = @Schema(
                    implementation = PrefixResponseDto.class)))
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
    @GET
    @Path("/{prefix-id}")
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    ),
                    @ParamRef(
                            param = "prefix-id",
                            type = ParamType.PATH,
                            referTo = PrefixResource.class
                    )
            },
            scope = {Scope.ALL, Scope.MINE}
    )
    public Response getById(
            @PathParam("id") int id,
            @PathParam("prefix-id") Integer prefixId) {

        return Response.ok(prefixService.fetchById(prefixId)).build();
    }

    @Operation(summary = "Get PID count")
    @APIResponse(responseCode = "200", description = "Count retrieved")
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
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @GET
    @Path("/{prefix-id}/count")
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    ),
                    @ParamRef(
                            param = "prefix-id",
                            type = ParamType.PATH,
                            referTo = PrefixResource.class
                    )
            },
            scope = {Scope.ALL, Scope.MINE}
    )
    public Response getPIDCountByPrefix(
            @PathParam("id") int id,
            @PathParam("prefix-id") String prefixId) {

        return Response.ok(statisticsService.getPIDCountByPrefixID(prefixId)).build();
    }

    @Operation(summary = "Get resolvable PID count")
    @APIResponse(responseCode = "200", description = "Count retrieved")
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
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @GET
    @Path("/{prefix-id}/resolvable")
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    ),
                    @ParamRef(
                            param = "prefix-id",
                            type = ParamType.PATH,
                            referTo = PrefixResource.class
                    )
            },
            scope = {Scope.ALL, Scope.MINE}
    )
    public Response getResolvablePIDCountByPrefix(
            @PathParam("id") int id,
            @PathParam("prefix-id") String prefixId) {

        return Response.ok(statisticsService.getResolvablePIDCountByPrefixID(prefixId)).build();
    }

    @Operation(summary = "Get prefix statistic")
    @APIResponse(
            responseCode = "200",
            description = "Statistics retrieved",
            content = @Content(schema = @Schema(
                    implementation = StatisticsDto.class)))
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
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @GET
    @Path("/{prefix-id}/statistic")
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    ),
                    @ParamRef(
                            param = "prefix-id",
                            type = ParamType.PATH,
                            referTo = PrefixResource.class
                    )
            },
            scope = {Scope.ALL, Scope.MINE}
    )
    public Response getStatisticsByPrefix(
            @PathParam("id") int id,
            @PathParam("prefix-id") String prefixId) {

    return Response.ok(statisticsService.getPrefixStatisticsByID(prefixId)).build();
  }

    @Operation(summary = "Set prefix statistic")
    @APIResponse(
            responseCode = "200",
            description = "Statistics saved",
            content = @Content(schema = @Schema(
                    implementation = StatisticsDto.class)))
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
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @POST
    @Path("/{prefix-id}/statistic")
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    ),
                    @ParamRef(
                            param = "prefix-id",
                            type = ParamType.PATH,
                            referTo = PrefixResource.class
                    )
            }
    )
    public Response setStatisticsByPrefix(
            @PathParam("id") int id,
            @PathParam("prefix-id") String prefixId,
            StatisticsRequestDto statisticsRequestDto) {

    return Response.ok(statisticsService.setPrefixStatistics(prefixId, statisticsRequestDto)).build();
  }

    public static class PageableObjects extends PageResource<PrefixResponseDto> {

        private List<PrefixResponseDto> content;

        @Override
        public List<PrefixResponseDto> getContent() {
            return content;
        }

        @Override
        public void setContent(List<PrefixResponseDto> content) {
            this.content = content;
        }
    }
}