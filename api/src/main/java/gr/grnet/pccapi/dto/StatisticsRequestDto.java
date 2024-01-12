package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Accessors(chain = true)
public class StatisticsRequestDto {
  @Schema(
      type = SchemaType.INTEGER,
      implementation = Integer.class,
      description = "The total number of handles in the prefix.",
      example = "1")
  @JsonProperty("handles_count")
  public Integer handlesCount;

  @Schema(
      type = SchemaType.INTEGER,
      implementation = Integer.class,
      description = "The resolvable handles count.",
      example = "1")
  @JsonProperty("resolvable_count")
  public Integer resolvableCount;

  @Schema(
      type = SchemaType.INTEGER,
      implementation = Integer.class,
      description = "The unresolvable handles count.",
      example = "1")
  @JsonProperty("unresolvable_count")
  public Integer unresolvableCount;

  @Schema(
      type = SchemaType.INTEGER,
      implementation = Integer.class,
      description = "The unchecked handles count.",
      example = "1")
  @JsonProperty("unchecked_count")
  public Integer uncheckedCount;
}
