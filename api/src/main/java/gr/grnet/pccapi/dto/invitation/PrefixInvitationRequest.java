package gr.grnet.pccapi.dto.invitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class PrefixInvitationRequest {

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
            type = SchemaType.OBJECT,
            implementation = String.class,
            description = "Provider role",
            example = "admin"
    )
    @JsonProperty("role")
    @NotEmpty(message = "role must not be empty")
    public String role;
}
