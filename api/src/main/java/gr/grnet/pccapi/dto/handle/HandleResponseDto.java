package gr.grnet.pccapi.dto.handle;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Represents a single Handle with its corresponding values.")
@Setter
@Getter
@Accessors(chain = true)
@EqualsAndHashCode
public class HandleResponseDto {

  @Schema(
          type = SchemaType.STRING,
          implementation = String.class,
          description = "The name of the Handle.",
          example = "21.T15999/NI4OS-EURO")
  @JsonProperty("handle")
  private String handle;

  @Schema(
          type = SchemaType.ARRAY,
          implementation = HandleValueResponseDto.class,
          description = "A list of values registered under the Handle.")
  @JsonProperty("values")
  private List<HandleValueResponseDto> values = new ArrayList<>();

  public void addType(String type, String value) {
    values.add(new HandleValueResponseDto(null, type, new HandleDataResponseDto("string", value), null, null));
  }

}