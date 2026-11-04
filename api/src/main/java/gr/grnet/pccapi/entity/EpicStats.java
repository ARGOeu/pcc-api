
package gr.grnet.pccapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "prefix_epic_stats")
public class EpicStats {

    @Id
    @Column(name = "prefix_id", nullable = false)
    public Integer prefixId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "prefix_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey =
            @ForeignKey(name = "fk_prefix_epic_stats_prefix"))
    public Prefix prefix;

    @Column(name = "stats_value")
    public Long statsValue;

    @Column(name = "handle_timestamp")
    public Instant handleTimestamp;

    @Column(name = "status", nullable = false, length = 16)
    public String status = "PENDING";

    @Column(name = "last_checked_at")
    public Instant lastCheckedAt;

    @Column(name = "last_success_at")
    public Instant lastSuccessAt;
}
