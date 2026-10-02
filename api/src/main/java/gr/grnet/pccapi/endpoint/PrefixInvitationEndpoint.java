package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.dto.invitation.PrefixInvitationRequest;
import gr.grnet.pccapi.dto.invitation.PrefixInvitationResponse;
import gr.grnet.pccapi.dto.pagination.PageResource;
import gr.grnet.pccapi.repository.PrefixInvitationRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.resources.PrefixResource;
import gr.grnet.pccapi.resources.ProviderResource;
import gr.grnet.pccapi.service.PrefixInvitationService;
import gr.grnet.pccapi.service.Utility;
import gr.grnet.pccapi.validator.constraints.NotFoundEntity;
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
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.endpoint.scanner.runtime.ParamRef;
import org.grnet.endpoint.scanner.runtime.ParamType;
import org.grnet.endpoint.scanner.runtime.Scope;
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;

import java.util.List;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Tag(name = "Prefix Invitation")
@Path("/providers/{id}/prefixes/{prefix-id}/invitations")
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
public class PrefixInvitationEndpoint {

    @Inject
    PrefixInvitationService prefixInvitationService;

    @Inject
    Utility utility;

    @Operation(
            summary = "Create prefix invitation",
            description = "Creates an invitation for the specified Prefix."
    )
    @APIResponse(
            responseCode = "201",
            description = "Invitation created.",
            content = @Content(schema = @Schema(
                    implementation = PrefixInvitationResponse.class)))
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
            description = "Invitation already exists.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @POST
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
    public Response createInvitation(
            @Parameter(
                    description = "Provider identifier.",
                    required = true,
                    example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("id")
            @NotFoundEntity(repository = ProviderRepository.class, message = "There is no Provider with the following id: ")
            Integer id,
            @Parameter(
                    description = "Prefix identifier.",
                    required = true,
                    example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("prefix-id")
            @NotFoundEntity(repository = PrefixRepository.class, message = "There is no Prefix with the following id: ")
            Integer prefixId,
            @Valid PrefixInvitationRequest request) {

        var response = prefixInvitationService.createInvitation(prefixId, request, utility.getUserUniqueIdentifier());

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @Operation(summary = "Get prefix invitations")
    @APIResponse(
            responseCode = "200",
            description = "Invitations found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageablePrefixInvitations.class)))
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
    public Response getInvitations(
            @Parameter(description = "Provider identifier.",
                    required = true, example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = ProviderRepository.class, message = "There is no Provider with the following id: ")
            Integer id,
            @Parameter(description = "Prefix identifier.",
                    required = true, example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("prefix-id")
            @Valid @NotFoundEntity(repository = PrefixRepository.class, message = "There is no Prefix with the following id: ")
            Integer prefixId,
            @Parameter(name = "search", in = QUERY,
                    description = "Search invitations by role or email.",
                    example = "user@grnet.gr")
            @QueryParam("search")
            String search,
            @Parameter(
                    name = "sort",
                    in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = "createdAt"),
                    examples = {
                            @ExampleObject(name = "Created At", value = "createdAt"),
                            @ExampleObject(name = "Email", value = "email"),
                            @ExampleObject(name = "Status", value = "status")
                    },
                    description = "The field used to sort the results.")
            @DefaultValue("createdAt")
            @QueryParam("sort")
            String sort,
            @Parameter(
                    name = "order",
                    in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = "DESC"),
                    examples = {
                            @ExampleObject(name = "Ascending", value = "ASC"),
                            @ExampleObject(name = "Descending", value = "DESC")
                    },
                    description = "The order of the sorted results.")
            @DefaultValue("DESC")
            @QueryParam("order")
            String order,
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

        var response = prefixInvitationService.getInvitationsByPrefixByPageAndSize(search, sort, order, prefixId, page - 1, size, uriInfo);

        return Response.ok(response).build();
    }

    @Operation(summary = "Get prefix invitation by id",
            description = "Returns a specific invitation belonging to the specified Prefix.")
    @APIResponse(
            responseCode = "200",
            description = "Invitation found.",
            content = @Content(schema = @Schema(
                    implementation = PrefixInvitationResponse.class)))
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
            description = "Invitation not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @GET
    @Path("/{invitation_id}")
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
    public Response getInvitationById(
            @Parameter(description = "Provider identifier.",
                    required = true,
                    example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = ProviderRepository.class, message = "There is no Provider with the following id: ")
            Integer id,
            @Parameter(description = "Prefix identifier.",
                    required = true,
                    example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("prefix-id")
            @Valid @NotFoundEntity(repository = PrefixRepository.class, message = "There is no Prefix with the following id: ")
            Integer prefixId,
            @Parameter(description = "Invitation identifier.",
                    required = true, example = "7d9c82ee-6d39-4d47-a42f-1c91e91777c3",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("invitation_id")
            @Valid @NotFoundEntity(repository = PrefixInvitationRepository.class, message = "There is no Invitation with the following invitation_id: ")
            String invitationId) {

        return Response.ok(prefixInvitationService.getInvitationById(prefixId, invitationId)).build();
    }

    @Operation(summary = "Revoke prefix invitation", description = "Revokes an invitation belonging to the specified Prefix.")
    @APIResponse(
            responseCode = "200",
            description = "Invitation revoked.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PrefixInvitationResponse.class)))
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
            description = "Invitation not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "Invitation already responded.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @PATCH
    @Path("/{invitation_id}")
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
    public Response revoke(
            @Parameter(description = "Provider identifier.",
                    required = true,
                    example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = ProviderRepository.class, message = "There is no Provider with the following id: ")
            Integer id,
            @Parameter(description = "Prefix identifier.",
                    required = true, example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("prefix-id")
            @Valid @NotFoundEntity(repository = PrefixRepository.class, message = "There is no Prefix with the following id: ")
            Integer prefixId,
            @Parameter(description = "Invitation identifier.",
                    required = true, example = "7d9c82ee-6d39-4d47-a42f-1c91e91777c3",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("invitation_id")
            @Valid @NotFoundEntity(repository = PrefixInvitationRepository.class, message = "There is no Invitation with the following invitation_id: ")
            String invitationId) {

        var response = prefixInvitationService.revokeInvitation(prefixId, invitationId, utility.getUserUniqueIdentifier());

        return Response.ok(response).build();
    }

    public static class PageablePrefixInvitations extends PageResource<PrefixInvitationResponse> {

        private List<PrefixInvitationResponse> content;

        @Override
        public List<PrefixInvitationResponse> getContent() {return content;}

        @Override
        public void setContent(List<PrefixInvitationResponse> content) {this.content = content;}
    }
}