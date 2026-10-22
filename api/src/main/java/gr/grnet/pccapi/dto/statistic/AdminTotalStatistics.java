package gr.grnet.pccapi.dto.statistic;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(name="AdminTotalStatistics", description="An object representing AdminTotalStatistics.")
public class AdminTotalStatistics {

    @Schema(
            type = SchemaType.NUMBER,
            implementation = Long.class,
            description = "The number of total providers.",
            example = "10"
    )
    @JsonProperty("total_providers")
    public Long  totalProvidersNum;

    @Schema(
            type = SchemaType.NUMBER,
            implementation = Long.class,
            description = "The number of total prefixes.",
            example = "10"
    )

    @JsonProperty("total_prefixes")
    public Long  totalPrefixesNum;

    @Schema(
            type = SchemaType.NUMBER,
            implementation = Long.class,
            description = "The number of total accounts.",
            example = "10"
    )

    @JsonProperty("total_accounts")
    public Long  totalAccountsNum;

    @Schema(
            type = SchemaType.NUMBER,
            implementation = List.class,
            description = "The number of prefixes per provider.",
            example = "10"
    )
    @JsonProperty("prefixes_per_provider")
    public List<PrefixesPerProviderDto> prefixesPerProvider;

    @Schema(
            type = SchemaType.NUMBER,
            implementation = List.class,
            description = "The number of accounts per provider.",
            example = "10"
    )
    @JsonProperty("accounts_per_provider")
    public List<AccountsPerProviderDto> accountsPerProvider;

}
