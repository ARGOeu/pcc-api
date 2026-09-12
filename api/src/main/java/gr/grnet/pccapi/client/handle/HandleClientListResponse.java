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

    @JsonProperty("total_count")
    private Long totalCount;

    @JsonProperty("page")
    private Integer page;

    @JsonProperty("page_size")
    private Integer size;

    @JsonProperty("handles")
    private List<String> handles;
}
