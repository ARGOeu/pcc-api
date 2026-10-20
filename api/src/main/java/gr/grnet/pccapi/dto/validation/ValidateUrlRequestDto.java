package gr.grnet.pccapi.dto.validation;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Accessors(chain = true)
@Schema(name = "ValidateUrlRequest")
public class ValidateUrlRequestDto {

    @NotBlank
    @Schema(
            type = SchemaType.STRING,
            description = "The URL to validate.",
            example = "https://example.com/resource")
    public String url;
}