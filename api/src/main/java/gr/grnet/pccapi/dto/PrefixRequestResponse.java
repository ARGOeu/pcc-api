package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.grnet.pccapi.enums.PrefixRequestStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrefixRequestResponse {

    @Schema(
            type = SchemaType.INTEGER,
            implementation = Integer.class,
            description = "The unique ID of the prefix request",
            example = "1"
    )
    public Integer id;

    @Schema(
            type = SchemaType.STRING,
            description = "Contact person's first name",
            example = "John"
    )
    @JsonProperty("contact_person_name")
    public String contactPersonName;

    @Schema(
            type = SchemaType.STRING,
            description = "Contact person's surname",
            example = "Doe"
    )
    @JsonProperty("contact_person_surname")
    public String contactPersonSurname;

    @Schema(
            type = SchemaType.STRING,
            description = "Contact person's email",
            example = "john.doe@example.org"
    )
    @JsonProperty("contact_person_email")
    public String contactPersonEmail;

    @Schema(
            type = SchemaType.STRING,
            description = "Contact person's phone number",
            example = "+30 210 1234567"
    )
    @JsonProperty("contact_person_phone")
    public String contactPersonPhone;

    @Schema(
            type = SchemaType.STRING,
            description = "Country of the organization",
            example = "Greece"
    )
    public String country;

    @Schema(
            type = SchemaType.STRING,
            description = "Name of the organization",
            example = "Example University"
    )
    @JsonProperty("organization_name")
    public String organizationName;

    @Schema(
            type = SchemaType.STRING,
            description = "Name of the project",
            example = "Research Project"
    )
    @JsonProperty("project_name")
    public String projectName;

    @Schema(
            type = SchemaType.STRING,
            description = "URL of the project",
            example = "https://example.org"
    )
    @JsonProperty("project_url")
    public String projectUrl;

    @Schema(
            type = SchemaType.STRING,
            description = "Description of the project and intended use of the EPIC service",
            example = "Using the EPIC service for persistent identifiers"
    )
    @JsonProperty("project_description")
    public String projectDescription;

    @Schema(
            type = SchemaType.STRING,
            description = "Expected number of handles",
            example = "UP_TO_1000"
    )
    @JsonProperty("expected_handles")
    public String expectedHandles;

    @Schema(
            type = SchemaType.STRING,
            implementation = PrefixRequestStatus.class,
            description = "Current status of the request",
            example = "PENDING"
    )
    public PrefixRequestStatus status;

    @Schema(
            type = SchemaType.STRING,
            description = "Reason for accepting or rejecting the request",
            example = "The request meets the requirements"
    )
    @JsonProperty("status_reason")
    public String statusReason;

    @Schema(
            type = SchemaType.INTEGER,
            description = "ID of the assigned provider",
            example = "1"
    )
    @JsonProperty("provider_id")
    public Integer providerId;

    @Schema(
            type = SchemaType.STRING,
            description = "Name of the assigned provider",
            example = "GRNET"
    )

    @JsonProperty("provider_name")
    public String providerName;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of creation",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty("created_at")
    public Instant createdAt;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of request's update",
            example = "2025-10-22T13:44:48.107Z"
    )
    @JsonProperty("updated_at")
    public Instant updatedAt;

    @Schema(
            type = SchemaType.STRING,
            description = "Creator's id",
            example = "userid1233"
    )

    @JsonProperty("created_by")
    public String createdBy;

    @Schema(
            type = SchemaType.STRING,
            description = "Adm handling the request",
            example = "admin1234"
    )

    @JsonProperty("handled_by")
    public String handledBy;

}
