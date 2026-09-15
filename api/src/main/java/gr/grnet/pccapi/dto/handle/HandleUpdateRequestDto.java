package gr.grnet.pccapi.dto.handle;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Request for updating an existing Handle.",
        example = """
                {
                  "values": [
                    {
                      "type": "URL",
                      "value": "https://example.com/updated"
                    },
                    {
                      "type": "title",
                      "value": "Updated Handle"
                    },
                    {
                      "type": "publisher",
                      "value": "GRNET"
                    }
                  ]
                }
                """)
@Getter
@Setter
@Accessors(chain = true)
public class HandleUpdateRequestDto {

    @Valid
    @NotEmpty
    @JsonProperty("values")
    private List<HandleTypeValue> values = new ArrayList<>();
}