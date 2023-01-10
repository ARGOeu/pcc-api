package gr.grnet.pccapi.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Key value pairs that represent filters with their corresponding values.")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class FiltersDto {
  @Schema(description = "Key value pairs that represent filters with their corresponding values.")
  public Map<String, String> filters;
}
