package gr.grnet.pccapi.dto.handle;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
public class HandleListResponseDto {

    @Schema(
            type = SchemaType.STRING,
            description = "The PID Prefix.",
            example = "21.T15999")
    private String prefix;

    @JsonProperty("total_count")
    @Schema(
            description = "The total number of Handles registered under the Prefix.",
            example = "9922")
    private Long totalCount;

    @JsonProperty("page")
    @Schema(
            description = "The current page.",
            example = "0")
    private Integer page;

    @JsonProperty("size")
    @Schema(
            description = "The number of Handles returned per page.",
            example = "10")
    private Integer size;

    @Schema(
            type = SchemaType.ARRAY,
            implementation = String.class,
            description = "The Handle identifiers.")
    private List<String> handles;
}