
package gr.grnet.pccapi.dto.prefix;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Accessors(chain = true)
@Schema(name = "EpicStats")
public class EpicStatsDto {

    @Schema(
            type = SchemaType.INTEGER,
            implementation = Long.class,
            description = "The latest STATS value retrieved from the EPIC_STATS Handle.",
            example = "88",
            nullable = true)
    public Long stats;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The synchronization status: PENDING, FOUND, NOT_FOUND, NO_STATS, or ERROR.",
            example = "FOUND")
    public String status;

    @Schema(
            type = SchemaType.STRING,
            implementation = Instant.class,
            description = "The timestamp of the STATS value in the Handle Service.",
            example = "2026-02-04T23:30:08Z",
            nullable = true)
    @JsonProperty("handle_timestamp")
    public Instant handleTimestamp;

    @Schema(
            type = SchemaType.STRING,
            implementation = Instant.class,
            description = "The timestamp of the latest synchronization attempt.",
            example = "2026-11-04T00:00:00Z",
            nullable = true)
    @JsonProperty("last_checked_at")
    public Instant lastCheckedAt;

    @Schema(
            type = SchemaType.STRING,
            implementation = Instant.class,
            description = "The timestamp of the last successful synchronization.",
            example = "2026-11-04T00:00:00Z",
            nullable = true)
    @JsonProperty("last_success_at")
    public Instant lastSuccessAt;
}
