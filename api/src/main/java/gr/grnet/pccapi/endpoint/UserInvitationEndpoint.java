package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.dto.invitation.PrefixInvitationActionResponse;
import gr.grnet.pccapi.dto.invitation.PrefixInvitationResponse;
import gr.grnet.pccapi.dto.pagination.PageResource;
import gr.grnet.pccapi.repository.PrefixInvitationRepository;
import gr.grnet.pccapi.resources.InvitationResource;
import gr.grnet.pccapi.service.PrefixInvitationService;
import gr.grnet.pccapi.service.Utility;
import gr.grnet.pccapi.validator.constraints.NotFoundEntity;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.endpoint.scanner.runtime.ParamRef;
import org.grnet.endpoint.scanner.runtime.ParamType;
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;

import java.util.List;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Path("/users/invitation")
@Authenticated
@SecurityScheme(
        securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
@Tag(name = "User")
public class UserInvitationEndpoint {

    @Inject
    PrefixInvitationService prefixInvitationService;

    @Inject
    Utility utility;

    @Operation(
            summary = "Get invitation details.",
            description = "Returns invitation details for the authenticated user."
    )
    @APIResponse(
            responseCode = "200",
            description = "Invitation details.",
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
            responseCode = "410",
            description = "Invitation expired.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInvitationById(
            @PathParam("id")
            @Valid @NotFoundEntity(repository = PrefixInvitationRepository.class, message = "There is no Invitation with the following id: ")
            String id)  {

        var response = prefixInvitationService.getInvitationById(id, utility.getUserEmail());

        return Response.ok(response).build();
    }

    @Operation(
            summary = "Respond to invitation.",
            description = "Accepts or rejects an invitation for the authenticated user."
    )
    @APIResponse(
            responseCode = "200",
            description = "Invitation updated.",
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
    @APIResponse(
            responseCode = "410",
            description = "Invitation expired.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response respond(
            @PathParam("id")
            @Valid @NotFoundEntity(repository = PrefixInvitationRepository.class, message = "There is no Invitation with the following id: ") String id,
            @Valid @NotNull(message = "The request body is empty.")
            PrefixInvitationActionResponse request) {

        var response = prefixInvitationService.respondToInvitation(id, request, utility.getUserEmail(), utility.getUserUniqueIdentifier(), utility.getUsername());

        return Response.ok(response).build();
    }

    @Operation(
            summary = "Get all invitation.",
            description = "Returns all invitation for the authenticated user. "
    )
    @APIResponse(
            responseCode = "200",
            description = "Invitation details.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableInvitations.class)))
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
            responseCode = "410",
            description = "Invitation expired.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInvites(
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
            int size, @Context UriInfo uriInfo)
    {
        var response = prefixInvitationService.getAllInvitationsByUser(utility.getUserEmail(), page - 1 , size, uriInfo);

        return Response.ok(response).build();
    }



    // --------------------------------------------------------------------------------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------------------------------------------------------------------------------
    public static class PageableInvitations extends PageResource<PrefixInvitationResponse> {

        private List<PrefixInvitationResponse> content;

        @Override
        public List<PrefixInvitationResponse> getContent() {
            return content;
        }

        @Override
        public void setContent(List<PrefixInvitationResponse> content) {
            this.content = content;
        }
    }
}
