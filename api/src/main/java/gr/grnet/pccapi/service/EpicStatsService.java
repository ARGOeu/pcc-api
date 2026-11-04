
package gr.grnet.pccapi.service;

import gr.grnet.pccapi.client.handle.HandleClient;
import gr.grnet.pccapi.repository.EpicStatsRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.WebApplicationException;
import java.net.URI;
import java.time.Instant;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EpicStatsService {

    private static final Logger LOG = Logger.getLogger(EpicStatsService.class);

    @Inject
    EntityManager entityManager;
    @Inject
    EpicStatsRepository epicStatsRepository;

    @Inject
    PrefixRepository prefixRepository;

    @ConfigProperty(name = "epic.stats.proxy-base-url", defaultValue = "https://hdl.handle.net")
    String proxyBaseUrl;

    private HandleClient handleClient;

    @PostConstruct
    void init() {
        handleClient = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(proxyBaseUrl))
                .build(HandleClient.class);
    }

    public void refreshAllPrefixes() {
        var prefixes = entityManager.createQuery(
                "SELECT p.id, p.name FROM prefix p",
                Object[].class
        ).getResultList();

        for (var prefix : prefixes) {
            refreshPrefix((Integer) prefix[0], (String) prefix[1]);
        }

        LOG.info("EPIC_STATS synchronization finished");
    }

    public void refreshPrefix(Integer prefixId, String prefixName) {
        try {
            var response = handleClient.getHandle(null, prefixName, "EPIC_STATS");

            if (response == null || !Integer.valueOf(1).equals(response.getResponseCode())) {
                throw new IllegalStateException("Unexpected EPIC_STATS response for " + prefixName);
            }

            var statsValue = response.getValues() == null ? null : response.getValues().stream()
                    .filter(value -> "STATS".equals(value.getType()))
                    .findFirst()
                    .orElse(null);


            var stats = Long.valueOf(statsValue.getData().getValue().toString());
            var timestamp = statsValue.getTimestamp() == null ? null : Instant.parse(statsValue.getTimestamp());

            epicStatsRepository.updateFound(prefixId, stats, timestamp);
            LOG.debugf("EPIC_STATS synchronized for prefix %s: %d", prefixName, stats);

        } catch (WebApplicationException e) {
            if (e.getResponse() != null && e.getResponse().getStatus() == 404) {
                epicStatsRepository.updateNotFound(prefixId);
                return;
            }

            LOG.errorf(e, "Failed to retrieve EPIC_STATS for prefix %s", prefixName);
            epicStatsRepository.updateError(prefixId);

        } catch (Exception e) {
            LOG.errorf(e, "Failed to synchronize EPIC_STATS for prefix %s", prefixName);
            epicStatsRepository.updateError(prefixId);
        }
    }
}
