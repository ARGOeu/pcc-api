package gr.grnet.pccapi.dto.statistic;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name="AccountsPerProvider", description="An object representing accounts number per provider")
public class AccountsPerProviderDto {

    @Schema(
            type = SchemaType.NUMBER,
            implementation = Long.class,
            description = "The number of total accounts per provider.",
            example = "10"
    )
    @JsonProperty("total_accounts")
    public Long totalAccountsNum;


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The provider name",
            example = "10"
    )
    @JsonProperty("provider")
    public String provider;

    public AccountsPerProviderDto(String provider, Long totalAccountsNum) {
        this.provider = provider;
        this.totalAccountsNum = totalAccountsNum;
    }
}
