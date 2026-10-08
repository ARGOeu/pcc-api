package gr.grnet.pccapi.dto.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public class RoleAssignmentMetadataResponseDto {

    @Schema(
            type = SchemaType.OBJECT,
            implementation = Map.class,
            description = "Assignment attributes per resource type."
    )
    @JsonProperty("resources")
    public Map<String, List<MetadataAttributeDto>> resources;
}
