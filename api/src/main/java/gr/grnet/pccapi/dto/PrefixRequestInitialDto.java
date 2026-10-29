package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.grnet.pccapi.validator.constraints.ValidExpectedHandles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.URL;


@Getter
@Setter
public class PrefixRequestInitialDto {

    @Schema(
            type = SchemaType.NUMBER,
            description = "Prefix Request's id",
            example = "1"
    )
    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    public Long id;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The name of the contact person.",
            example = "John")
    @NotBlank(message = "Name URL must not be empty")
    @JsonProperty("contact_person_name")
    private String contactPersonName;


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The surnname of the contact person.",
            example = "Doe")
    @NotBlank(message = "Surnae must not be empty")
    @JsonProperty("contact_person_surname")
    private String contactPersonSurname;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The email of the contact person.",
            example = "JohnDoe@gmail.com")
    @NotBlank(message = "Contact email must not be empty")
    @Email(message = "Contact email must be a valid email address")
    @JsonProperty("contact_person_email")
    private String contactPersonEmail;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The phone number of the contact person.",
            example = "2103911103")
    @JsonProperty("contact_person_phone")
    private String contactPersonPhone;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The country of the contact person.",
            example = "Greece")
    @NotBlank(message = "Country must not be empty")
    private String country;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The organization name  of the contact person.",
            example = "GRNET")
    @NotBlank(message = "Organization name must not be empty")
    @JsonProperty("organization_name")
    private String organizationName;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The project name.",
            example = "PROJECT")
    @NotBlank(message = "Project name must not be empty")
    @JsonProperty("project_name")
    private String projectName;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The url of the project.",
            example = "https://project.org")
    @NotBlank(message = "Project URL must not be empty")
    @URL(message = "Project URL must be a valid URL")
    @JsonProperty("project_url")
    private String projectUrl;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The description of the project.",
            example = "This is the PROJECT description")
    @JsonProperty("project_description")
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
}


