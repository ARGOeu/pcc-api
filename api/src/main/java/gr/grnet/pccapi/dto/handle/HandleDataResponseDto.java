package gr.grnet.pccapi.dto.handle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class HandleDataResponseDto {

    @Schema(
            type = SchemaType.STRING,
            description = "The format of the Handle value.",
            example = "string")
    @JsonProperty("format")
    private String format;

    @Schema(
            description = "The stored Handle value.",
            example = "https://example.com")
    @JsonProperty("value")
    private Object value;
}