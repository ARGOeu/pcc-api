package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.dto.invitation.InvitationRequest;
import gr.grnet.pccapi.dto.invitation.InvitationResponse;
import gr.grnet.pccapi.dto.pagination.PageResource;
import gr.grnet.pccapi.repository.InvitationRepository;
import gr.grnet.pccapi.service.InvitationService;
import gr.grnet.pccapi.service.Utility;
import gr.grnet.pccapi.validator.constraints.NotFoundEntity;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
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
import org.grnet.endpoint.scanner.runtime.Scope;
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;

import java.util.List;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Tag(name = "Invitation")
@Path("/invitations")
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
public class InvitationEndpoint {

    @Inject
    InvitationService invitationService;

    @Inject
    Utility utility;

    // ---------------------------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------------------------

    @Operation(
            summary = "Create invitation",
            description = "Creates an invitation for the specified API resource."
    )
    @APIResponse(
            responseCode = "201",
            description = "Invitation created.",
            content = @Content(schema = @Schema(
                    implementation = InvitationResponse.class
            ))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid request.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            ))
    )
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            ))
    )
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            ))
    )
    @APIResponse(
            responseCode = "404",
            description = "Target entity not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            ))
    )
    @APIResponse(
            responseCode = "409",
            description = "Invitation already exists.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            ))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class
            ))
    )
    @SecurityRequirement(name = "Authentication")
    @POST
    @SecuredEndpoint
    public Response createInvitation(
            @Valid InvitationRequest request) {

        var response = invitationService.createInvitation(request, utility.getUserUniqueIdentifier());

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    // ---------------------------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------------------------

    @Operation(
            summary = "Get invitations",
            description = "Returns invitations. Results can optionally be filtered by API resource and resource identifier."
    )
    @APIResponse(
            responseCode = "200",
            description = "Invitations found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableInvitations.class
            ))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid request.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            ))
    )
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            ))
    )
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            ))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class
            ))
    )
    @SecurityRequirement(name = "Authentication")
    @GET
    @SecuredEndpoint(scope = {Scope.ALL, Scope.MINE})
    public Response getInvitations(
            @Parameter(name = "api_resource", in = QUERY,
                    description = "Filter invitations by API resource.",
                    example = "Provider")
            @QueryParam("api_resource")
            String apiResource,
            @Parameter(name = "resource_id", in = QUERY,
                    description = "Filter invitations by resource identifier.",
                    example = "1")
            @QueryParam("resource_id")
            String resourceId,
            @Parameter(name = "search", in = QUERY,
                    description = "Search invitations by role or email.",
                    example = "user@grnet.gr")
            @QueryParam("search")
            String search,
            @Parameter(name = "sort", in = QUERY,
                    schema = @Schema(
                            type = SchemaType.STRING,
                            defaultValue = "createdAt"),
                    examples = {
                            @ExampleObject(
                                    name = "Created At",
                                    value = "createdAt"
                            ),
                            @ExampleObject(
                                    name = "Email",
                                    value = "email"
                            ),
                            @ExampleObject(
                                    name = "Status",
                                    value = "status"
                            )
                    },
                    description = "The field used to sort the results."
            )
            @DefaultValue("createdAt")
            @QueryParam("sort")
            String sort,
            @Parameter(name = "order", in = QUERY,
                    schema = @Schema(
                            type = SchemaType.STRING,
                            defaultValue = "DESC"),
                    examples = {
                            @ExampleObject(
                                    name = "Ascending",
                                    value = "ASC"),
                            @ExampleObject(
                                    name = "Descending",
                                    value = "DESC")
                    },
                    description = "The order of the sorted results."
            )
            @DefaultValue("DESC")
            @QueryParam("order")
            String order,
            @Parameter(
                    name = "page",
                    in = QUERY,
                    description = "Page number. Must be >= 1."
            )
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
            int size, @Context UriInfo uriInfo) {

        var response = invitationService.getInvitationsByPageAndSize(apiResource, resourceId, search, sort, order, page - 1, size, uriInfo);

        return Response.ok(response).build();
    }

    // ---------------------------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------------------------

    @Operation(
            summary = "Get invitation by id",
            description = "Returns a specific invitation."
    )
    @APIResponse(
            responseCode = "200",
            description = "Invitation found.",
            content = @Content(schema = @Schema(
                    implementation = InvitationResponse.class
            ))
    )
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            ))
    )
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            ))
    )
    @APIResponse(
            responseCode = "404",
            description = "Invitation not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            ))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class
            ))
    )
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{invitation_id}")
    @SecuredEndpoint(scope = {Scope.ALL, Scope.MINE})
    public Response getInvitationById(
            @Parameter(
                    description = "Invitation identifier.",
                    required = true,
                    example = "7d9c82ee-6d39-4d47-a42f-1c91e91777c3",
                    schema = @Schema(type = SchemaType.STRING)
            )
            @PathParam("invitation_id")
            @Valid
            @NotFoundEntity(repository = InvitationRepository.class, message = "There is no Invitation with the following invitation_id: ")
            String invitationId) {

        var response = invitationService.getInvitationById(invitationId);

        return Response.ok(response).build();
    }

    // ---------------------------------------------------------------------------
    // REVOKE
    // ---------------------------------------------------------------------------

    @Operation(
            summary = "Revoke invitation",
            description = "Revokes a pending invitation."
    )
    @APIResponse(
            responseCode = "200",
            description = "Invitation revoked.",
            content = @Content(schema = @Schema(
                    implementation = InvitationResponse.class
            )))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            )))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            )))
    @APIResponse(
            responseCode = "404",
            description = "Invitation not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            )))
    @APIResponse(
            responseCode = "409",
            description = "Invitation already responded.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class
            )))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class
            )))
    @SecurityRequirement(name = "Authentication")
    @PATCH
    @Path("/{invitation_id}")
    @SecuredEndpoint
    public Response revoke(
            @Parameter(
                    description = "Invitation identifier.",
                    required = true,
                    example = "7d9c82ee-6d39-4d47-a42f-1c91e91777c3",
                    schema = @Schema(type = SchemaType.STRING)
            )
            @PathParam("invitation_id")
            @Valid
            @NotFoundEntity(repository = InvitationRepository.class, message = "There is no Invitation with the following invitation_id: ")
            String invitationId) {

        var response = invitationService.revokeInvitation(invitationId, utility.getUserUniqueIdentifier());

        return Response.ok(response).build();
    }

    // ---------------------------------------------------------------------------
    // PAGEABLE RESPONSE
    // ---------------------------------------------------------------------------

    public static class PageableInvitations extends PageResource<InvitationResponse> {

        private List<InvitationResponse> content;

        @Override
        public List<InvitationResponse> getContent() {
            return content;
        }

        @Override
        public void setContent(List<InvitationResponse> content) {
            this.content = content;
        }
    }
}