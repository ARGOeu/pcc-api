package gr.grnet.pccapi.client.handle;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Represents a request sent by the Handle client to create a Handle. */
@Getter
@AllArgsConstructor
public class HandleClientRequest {

    private List<Value> values;

    @Getter
    @AllArgsConstructor
    public static class Value {

        private Integer index;
        private String type;
        private Data data;
    }

    @Getter
    @AllArgsConstructor
    public static class Data {

        private String format;
        private Object value;
    }

    @Getter
    @AllArgsConstructor
    public static class AdminValue {

        private String handle;
        private Integer index;
        private String permissions;
    }
}