package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.dto.metadata.RoleAssignmentMetadataResponseDto;
import gr.grnet.pccapi.dto.metadata.RoleMetadataResponseDto;
import gr.grnet.pccapi.service.RoleMetadataService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;

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
}
