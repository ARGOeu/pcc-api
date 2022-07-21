package gr.grnet.pccapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**

 * APIError encapsulates all needed information that should return as a response to the API client
 * when an error has occurred.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Schema(name="APIResponseMsg", description="The generic API response message")
public class APIResponseMsg {
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "A message that informs whether a specific request has been completed.",
            example = "An informative message relative to the process"
    )
    private String message;
}
