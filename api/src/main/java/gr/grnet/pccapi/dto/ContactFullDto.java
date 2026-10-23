package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.grnet.pccapi.dto.provider.ProviderResponseDTO;
import gr.grnet.pccapi.entity.Contact;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(name = "ContactFullDto", description = "Represents the configuration of a contact info.")

public class ContactFullDto {
    @Schema(
            type = SchemaType.STRING,
            description = "Contacts's id",
            example = "1"
    )
    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)

    public Integer id;

    @Schema(
            type = SchemaType.STRING,
            description = "Contacts 's name",
            example = "Joe Doe"
    )
    @NotNull(message = "Name must not be null")
    @NotBlank(message = "Name must not be empty")
    @JsonProperty("name")
    public String name;

    @Schema(
            type = SchemaType.STRING,
            description = "Contacts's email",
            example = "joedoe@gmail.com"
    )
    @NotNull(message = "Email must not be null")
    @NotBlank(message = "Email must not be empty")
    @JsonProperty("email")
    public String email;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The contact type of the contact.",
            example = "ADMIN"
    )
    @JsonProperty("type")
    public String type;

    @Schema(
            type = SchemaType.ARRAY,
            implementation = List.class,
            description = "The List of Providers the contact is related with",
            example = "[{9e2c9890-56c7-432a-bd6e-32e1da6eaa84-5, Tenant A}, {9e2c9890-56c7-432a-bd6e-32e1da6eaa84-6,TenantB}]"
    )
    @JsonProperty("providers")

    public List<ProviderResponseDTO> providers;

    public ContactFullDto(Contact c, List<ProviderResponseDTO> orDefault) {
    }
}

