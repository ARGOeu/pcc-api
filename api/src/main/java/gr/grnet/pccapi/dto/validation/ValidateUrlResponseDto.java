package gr.grnet.pccapi.dto.validation;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Accessors(chain = true)
@Schema(name = "ValidateUrlResponse")
public class ValidateUrlResponseDto {

    @Schema(
            type = SchemaType.BOOLEAN,
            description = "Indicates whether the URL format is valid.",
            example = "true")
    public boolean valid;

    @Schema(
            type = SchemaType.BOOLEAN,
            description = "Indicates whether the URL is reachable.",
            example = "true")
    public boolean reachable;

    @Schema(
            type = SchemaType.STRING,
            description = "A message describing the validation result.",
            example = "URL is valid and reachable")
    public String message;
}