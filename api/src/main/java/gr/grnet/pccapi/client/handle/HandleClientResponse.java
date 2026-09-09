package gr.grnet.pccapi.client.handle;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HandleClientResponse {

    private Integer responseCode;
    private String handle;
    private String message;

}
