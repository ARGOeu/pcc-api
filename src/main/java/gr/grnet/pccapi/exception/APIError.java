package gr.grnet.pccapi.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**

 * APIError encapsulates all needed information that should return as a response to the API client
 * when an error has occurred.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class APIError {
    private String message;
}
