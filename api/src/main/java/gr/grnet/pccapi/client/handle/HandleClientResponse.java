package gr.grnet.pccapi.client.handle;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
public class HandleClientResponse {

    private Integer responseCode;
    private String handle;
    private String message;

    @JsonInclude(NON_NULL)
    private List<Value> values;

    @Getter
    @Setter
    public static class Value {

        private Integer index;
        private String type;
        private Data data;
        private Integer ttl;
        private String timestamp;
    }

    @Getter
    @Setter
    public static class Data {

        private String format;
        private Object value;
    }

}
