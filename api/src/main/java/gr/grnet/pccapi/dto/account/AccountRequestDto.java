package gr.grnet.pccapi.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class AccountRequestDto {

    @Email
    @NotBlank
    @Schema(description = "Email associated with the account.",
            example = "user@grnet.gr")
    @JsonProperty("email")
    public String email;

    @NotBlank
    @Schema(
            description = "Endpoint associated with the account.",
            example = "https://example.grnet.gr")
    @JsonProperty("endpoint")
    public String endpoint;
}