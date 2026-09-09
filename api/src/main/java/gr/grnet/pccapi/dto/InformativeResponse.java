package gr.grnet.pccapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Set;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
        name = "Response",
        description = "Illustrates if an API operation is successful or not.")
public class InformativeResponse {

    @Schema(
            type = SchemaType.NUMBER,
            implementation = Integer.class,
            description = "The HTTP status code.",
            example = "400")
    public int code;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "An informative message about the operation.",
            example = "The request could not be completed.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String message;

    @Schema(
            type = SchemaType.ARRAY,
            implementation = String.class,
            description = "List of error messages.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Set<String> errors;
}