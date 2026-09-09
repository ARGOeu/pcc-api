package gr.grnet.pccapi.dto.handle;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class HandleTypeValue {

    @NotBlank
    @Schema(
            type = SchemaType.STRING,
            description = "The type of the value.",
            example = "URL")
    @JsonProperty("type")
    private String type;

    @NotBlank
    @Schema(
            type = SchemaType.STRING,
            description = "The value itself.",
            example = "https://example.com")
    @JsonProperty("value")
    private String value;
}