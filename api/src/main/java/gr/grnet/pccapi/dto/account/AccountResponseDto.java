package gr.grnet.pccapi.dto.account;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class AccountResponseDto {

    @Schema(
            description = "Account identifier.",
            example = "7d9c82ee-6d39-4d47-a42f-1c91e91777c3")
    @JsonProperty("id")
    public String id;

    @Schema(
            description = "Identifier of the Prefix associated with the Account.",
            example = "1")
    @JsonProperty("prefix_id")
    public Integer prefixId;

    @Schema(
            description = "Email associated with the account.",
            example = "user@grnet.gr")
    @JsonProperty("email")
    public String email;

    @Schema(
            description = "Endpoint associated with the account.",
            example = "https://example.grnet.gr")
    @JsonProperty("endpoint")
    public String endpoint;

    @Schema(
            description = "Admin index assigned to the account.",
            example = "301")
    @JsonProperty("admin_index")
    public Integer adminIndex;

    @Schema(
            description = "Permissions assigned to the account.",
            example = "011111110011")
    @JsonProperty("permissions")
    public String permissions;

    @Schema(
            description = "Date and time when the account was created.",
            example = "2026-09-29T12:00:00Z")
    @JsonProperty("created_at")
    public Instant createdAt;
}