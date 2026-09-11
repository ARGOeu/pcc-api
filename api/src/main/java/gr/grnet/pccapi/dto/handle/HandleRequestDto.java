package gr.grnet.pccapi.dto.handle;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Schema(
        description = "Request for creating a Handle under an existing Prefix.",
        example = """
        {
          "suffix": "doe-test-1",
          "serviceUrl": "https://hdl.grnet.gr:8001",
          "token": "TOKEN",
          "values": [
            {
              "type": "URL",
              "value": "https://www.grnet.gr"
            },
            {
              "type": "title",
              "value": "Doe Test Handle"
            },
            {
              "type": "description",
              "value": "Handle created through PCC"
            }
          ]
        }
        """)
public class HandleRequestDto {

    @NotBlank
    @Schema(
            description = "The suffix of the Handle to create.",
            example = "doe-test-24")
    private String suffix;

    @NotBlank
    @Schema(
            description = "The URL of the Handle service.",
            example = "https://hdl.grnet.gr:8001")
    private String serviceUrl;

    @NotBlank
    @Schema(
            description = "The authentication token for the Handle service.",
            example = "TOKEN")
    private String token;

    @NotEmpty
    @Valid
    @Schema(description = "The values associated with the Handle.")
    private List<HandleTypeValue> values;
}