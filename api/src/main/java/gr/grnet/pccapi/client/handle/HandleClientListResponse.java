package gr.grnet.pccapi.client.handle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
public class HandleClientListResponse {

    private String prefix;

    @JsonProperty("totalCount")
    private Long totalCount;

    @JsonProperty("handles")
    private List<String> handles;
}
