package gr.grnet.pccapi.dto.statistic;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name="AdminStatisticsResponse", description="An object representing AdminStatisticsResponse.")
public class AdminStatisticsResponse {
    @Schema(
            type = SchemaType.NUMBER,
            implementation = AdminTotalStatistics.class,
            description = "The total statistics.",
            example = "{\n" +
                    "  \"total_statistics\": {\n" +
                    "    \"number_of_providers\": 5,\n" +
                    "    \"number_of_prefixes\": 10,\n"+
                    "    \"number_of_accounts\": 6,\n"+
                    "    \"prefixes_per_provider\": [\n" +
                    "      {\n" +
                    "        \"total_prefixes\": 8,\n" +
                    "        \"provider\": \"GRNET\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"accounts_per_provider\": [\n" +
                    "      {\n" +
                    "        \"total_accounts\": 5,\n" +
                    "        \"provider\": \"GRNET\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}"
    )
    @JsonProperty("total_statistics")
    public AdminTotalStatistics totalStatistics;
}
