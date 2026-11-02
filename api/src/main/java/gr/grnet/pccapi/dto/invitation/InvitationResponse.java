package gr.grnet.pccapi.dto.invitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.grnet.pccapi.enums.InvitationStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

public class InvitationResponse {
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Invitation Id"
    )
    @JsonProperty("id")
    public String id;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "API resource to which the user is being invited.",
            example = "Provider"
    )
    @JsonProperty("api_resource")
    public String apiResource;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Identifier of the target API resource.",
            example = "1"
    )
    @JsonProperty("resource_id")
    public String resourceId;

    @Schema(
            type = SchemaType.STRING,
            description = "Recipient's email",
            example = "grnet@gmail.com"
    )
    @Email(message = "Email must be a valid email address")
    @NotBlank(message = "Email must not be empty")
    @JsonProperty("email")
    public String email;
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Provider invitation role",
            example = "member",
            enumeration = {"admin", "member"}
    )
    @JsonProperty("role")
    public String role;

    @Schema(
            type = SchemaType.OBJECT,
            implementation = InvitationStatus.class,
            description = "Invitation status"
    )
    @JsonProperty("status")
    public InvitationStatus status;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of creation",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty("created_at")
    public Instant createdAt;

}
