package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.grnet.pccapi.validator.constraints.ValidExpectedHandles;
import gr.grnet.pccapi.validator.constraints.ValidPrefixRequestStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
public class PrefixRequestDto {

    @Schema(
            type = SchemaType.NUMBER,
            description = "Prefix Request's id",
            example = "1"
    )
    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    public Integer id;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The name of the contact person.",
            example = "John")
    private String contactPersonName;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The surnname of the contact person.",
            example = "Doe")
    private String contactPersonSurname;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The email of the contact person.",
            example = "JohnDoe@gmail.com")
    @NotBlank(message = "Contact email must not be empty")

    private String contactPersonEmail;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The phone number of the contact person.",
            example = "2103911103")
    private String contactPersonPhone;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The country of the contact person.",
            example = "Greece")
    private String country;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The organization name  of the contact person.",
            example = "GRNET")
    private String organizationName;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The project name.",
            example = "PROJECT")
    private String projectName;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The url of the project.",
            example = "https://project.org")
    private String projectUrl;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The description of the project.",
            example = "This is the PROJECT description")
    private String projectDescription;



    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The expected handles num of the prefix.",
            example = "UP_TO_1000"
    )
    @JsonProperty("expected_handles")
    @NotBlank(message = "Expected handles must not be empty")
    @ValidExpectedHandles
    public String expectedHandles;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The expected handles num of the prefix.",
            example = "UP_TO_1000"
    )
    @JsonProperty("status")
    @ValidPrefixRequestStatus
    public String status;
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The status reason of the prefix request.",
            example = "It is rejected as invalid")

    private String statusReason;
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The provider id of the prefix request.",
            example = "1")

    private Long providerId;
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The provider name of the prefix request.",
            example = "GRNET")

    private String providerName;
}

