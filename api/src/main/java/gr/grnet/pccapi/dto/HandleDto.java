package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    description =
        "Represents a single handle with all of each corresponding values with their types.")
@Setter
@Getter
@Accessors(chain = true)
@EqualsAndHashCode
public class HandleDto {

  @Schema(
      type = SchemaType.STRING,
      implementation = String.class,
      description = "The name of the handle.",
      example = "11000/EXAMPLE-JTD_22 504D2A-EF64-51EB-AC5D-8A91485E35B4")
  @JsonProperty("handle")
  public String handle;

  @Schema(
      type = SchemaType.ARRAY,
      implementation = HandleTypeValue.class,
      description = "A list of type and values")
  @JsonProperty("values")
  public List<HandleTypeValue> values = new ArrayList<>();

  public void addType(String type, String value) {
    this.values.add(new HandleTypeValue(type, value));
  }

  @AllArgsConstructor
  @Getter
  @EqualsAndHashCode
  static class HandleTypeValue {

    @Schema(
        type = SchemaType.STRING,
        implementation = String.class,
        description = "The type of the value.",
        example = "URL")
    @JsonProperty("type")
    public String type;

    @Schema(
        type = SchemaType.STRING,
        implementation = String.class,
        description = "The value itself.",
        example = "example.com")
    @JsonProperty("value")
    public String value;
  }
}
