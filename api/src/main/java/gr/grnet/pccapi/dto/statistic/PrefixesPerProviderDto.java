package gr.grnet.pccapi.dto.statistic;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name="PrefixesPerProvider", description="An object representing prefixes number per provider")
public class PrefixesPerProviderDto {

    @Schema(
            type = SchemaType.NUMBER,
            implementation = Long.class,
            description = "The number of total prefixes per provider.",
            example = "10"
    )
    @JsonProperty("total_prefixes")
    public Long totalPrefixesNum;


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The provider name",
            example = "10"
    )
    @JsonProperty("provider")
    public String provider;

    public PrefixesPerProviderDto(String provider,Long totalPrefixesNum) {
        this.totalPrefixesNum = totalPrefixesNum;
        this.provider = provider;
    }
}