package gr.grnet.pccapi.dto.handle;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HandleValueResponseDto {

    @Schema(
            type = SchemaType.INTEGER,
            description = "The index of the Handle value.",
            example = "1")
    @JsonProperty("index")
    private Integer index;

    @Schema(
            type = SchemaType.STRING,
            description = "The type of the Handle value.",
            example = "URL")
    @JsonProperty("type")
    private String type;

    @Schema(
            implementation = HandleDataResponseDto.class,
            description = "The data stored for the Handle value.")
    @JsonProperty("data")
    private HandleDataResponseDto data;

    @Schema(
            type = SchemaType.INTEGER,
            description = "The time-to-live of the Handle value in seconds.",
            example = "86400")
    @JsonProperty("ttl")
    private Integer ttl;

    @Schema(
            type = SchemaType.STRING,
            description = "The timestamp of the Handle value.",
            example = "2023-07-03T13:27:20Z")
    @JsonProperty("timestamp")
    private String timestamp;
}