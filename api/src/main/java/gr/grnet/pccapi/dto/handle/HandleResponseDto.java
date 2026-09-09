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

@Schema(description = "Represents a single handle with its corresponding values and types.")
@Setter
@Getter
@Accessors(chain = true)
@EqualsAndHashCode
public class HandleResponseDto {

  @Schema(
          type = SchemaType.STRING,
          implementation = String.class,
          description = "The name of the handle.",
          example = "21.T15999/NI4OS-EUROPE")
  @JsonProperty("handle")
  public String handle;

  @Schema(
          type = SchemaType.ARRAY,
          implementation = HandleTypeValue.class,
          description = "A list of type and value pairs.")
  @JsonProperty("values")
  public List<HandleTypeValue> values = new ArrayList<>();

  public void addType(String type, String value) {
    this.values.add(new HandleTypeValue(type, value));
  }
}