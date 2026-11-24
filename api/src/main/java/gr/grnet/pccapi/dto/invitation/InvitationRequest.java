package gr.grnet.pccapi.dto.invitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class InvitationRequest {

    @JsonProperty("email")
    @Schema(
            description = "Email address of the user to invite.",
            example = "user1@example.com"
    )
    @NotBlank
    @Email
    public String email;

    @JsonProperty("api_resource")
    @Schema(
            description = "API resource to which the user is being invited.",
            example = "Provider"
    )
    @NotBlank
    public String apiResource;

    @JsonProperty("resource_id")
    @Schema(
            description = "Identifier of the target API resource.",
            example = "1"
    )
    @NotBlank
    public String resourceId;

    @JsonProperty("role")
    @Schema(
            description = "Role that will be assigned if the invitation is accepted.",
            example = "provider_admin"
    )
    @NotBlank
    public String role;
}