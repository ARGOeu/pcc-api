package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.grnet.pccapi.validator.constraints.ValidContactType;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "ContactDto", description = "Represents the configuration of a contact info.")

public class ContactDto {
    @Schema(
            type = SchemaType.STRING,
            description = "Contacts's id",
            example = "9e2c9890-56c7-432a-bd6e-32e1da6eaa84-5"
    )
    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    public String id;

    @Schema(
            type = SchemaType.STRING,
            description = "Contacts 's name",
            example = "Joe Doe"
    )
    @NotBlank(message = "Contact name must not be empty")
    @JsonProperty("name")
    public String name;

    @Schema(
            type = SchemaType.STRING,
            description = "Contacts's email",
            example = "joedoe@gmail.com"
    )
    @NotBlank(message = "Contact email must not be empty")
    @JsonProperty("email")
    public String email;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The contact type of the contact.",
            example = "ADMIN"
    )
    @JsonProperty("type")
    @NotBlank(message = "Contact type must not be empty")
    @ValidContactType
    public String type;

}
