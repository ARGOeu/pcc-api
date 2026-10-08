package gr.grnet.pccapi.dto.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

public class RoleMetadataResponseDto {

    @Schema(
            type = SchemaType.ARRAY,
            implementation = MetadataAttributeDto.class,
            description = "Supported role attributes."
    )
    @JsonProperty("attributes")
    public List<MetadataAttributeDto> attributes;
}
