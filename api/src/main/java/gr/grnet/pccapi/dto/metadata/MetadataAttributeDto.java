package gr.grnet.pccapi.dto.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class MetadataAttributeDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Attribute key.",
            example = "preferred_name"
    )
    @JsonProperty("key")
    public String key;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Display label.",
            example = "Admin"
    )
    @JsonProperty("label")
    public String label;

    @Schema(
            type = SchemaType.BOOLEAN,
            implementation = Boolean.class,
            description = "Whether the attribute is required.",
            example = "true"
    )
    @JsonProperty("required")
    public boolean required;
}
