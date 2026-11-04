
package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.EpicStats;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;

@ApplicationScoped
public class EpicStatsRepository
        implements PanacheRepositoryBase<EpicStats, Integer> {

    @Transactional
    public void updateFound(Integer prefixId, Long stats, Instant handleTimestamp) {
        var entity = findByIdOptional(prefixId).orElse(null);
        var now = Instant.now();

        if (entity == null) {
            entity = new EpicStats().setPrefixId(prefixId);
            persist(entity);
        }

        entity.statsValue = stats;
        entity.handleTimestamp = handleTimestamp;
        entity.status = "FOUND";
        entity.lastCheckedAt = now;
        entity.lastSuccessAt = now;
    }

    @Transactional
    public void updateNotFound(Integer prefixId) {
        var entity = findById(prefixId);
        var now = Instant.now();

        if (entity == null) {
            entity = new EpicStats()
                    .setPrefixId(prefixId)
                    .setStatus("NOT_FOUND")
                    .setLastCheckedAt(now);

            persist(entity);
            return;
        }

        entity.statsValue = null;
        entity.handleTimestamp = null;
        entity.status = "NOT_FOUND";
        entity.lastCheckedAt = now;
    }

    @Transactional
    public void updateError(Integer prefixId) {
        var entity = findById(prefixId);

        if (entity == null) {
            entity = new EpicStats().setPrefixId(prefixId);
            persist(entity);
        }

        entity.statsValue = null;
        entity.handleTimestamp = null;
        entity.status = "ERROR";
        entity.lastCheckedAt = Instant.now();
    }
}
