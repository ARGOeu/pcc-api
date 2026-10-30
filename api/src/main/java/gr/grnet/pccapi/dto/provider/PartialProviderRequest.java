package gr.grnet.pccapi.dto.provider;


import com.fasterxml.jackson.annotation.JsonProperty;
import gr.grnet.pccapi.dto.ContactDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(name = "PartialProviderRequest", description = "Represents the configuration of an update on provider.")
@Getter
@Setter
public class PartialProviderRequest {
    @Schema(
            type = SchemaType.STRING,
            description = "The description of the provider .",
            example = "GRNET provider description"
    )
    @JsonProperty("description")
    public String  description;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Provider's logo URL or base64 encoded image data URI. " +
                    "Example URL: https://example.com/logo.png, or data URI: data:image/png;base64,iVBORw0KGgo...",
            example = "https://example.com/logo.png")
    @Pattern(
            regexp = "(^https?://.*\\.(jpg|jpeg|png|gif)(\\?.*)?$)|(^data:image\\/([a-zA-Z]+);base64,[A-Za-z0-9+/=]+$)",
            message = "Must be a valid logo URL (jpg, jpeg, png, gif) or base64 encoded data URI",
            flags = Pattern.Flag.CASE_INSENSITIVE)
    @JsonProperty("logo")
    public String logo;


    @JsonProperty("contacts")
    @NotEmpty(message = "At least one contact should be added")
    @Valid
    public List<ContactDto> contacts;


}