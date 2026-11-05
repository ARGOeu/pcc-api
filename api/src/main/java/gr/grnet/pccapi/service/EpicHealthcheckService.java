package gr.grnet.pccapi.service;

import gr.grnet.pccapi.client.handle.HandleClient;
import gr.grnet.pccapi.repository.PrefixRepository;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;

@ApplicationScoped
public class EpicHealthcheckService {

    private static final Logger LOG = Logger.getLogger(EpicHealthcheckService.class);

    @Inject
    EntityManager entityManager;

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

    @Transactional
    public void refreshAllPrefixes() {
        var prefixes = entityManager.createQuery(
                "SELECT p.id, p.name FROM prefix p",
                Object[].class
        ).getResultList();

        LOG.infof("Starting EPIC_HEALTHCHECK synchronization for %d prefixes",
                prefixes.size());

        for (var prefix : prefixes) {
            refreshPrefix((Integer) prefix[0], (String) prefix[1]);
        }

        LOG.info("EPIC_HEALTHCHECK synchronization finished");
    }

    @Transactional
    public void refreshPrefix(Integer prefixId, String prefixName) {
        try {
            var response = handleClient.getHandle(
                    null, prefixName, "EPIC_HEALTHCHECK"
            );

            if (response == null || response.getResponseCode() == null) {
                throw new IllegalStateException(
                        "Invalid EPIC_HEALTHCHECK response for " + prefixName);
            }

            int code = response.getResponseCode();

            if (code == 1) {
                prefixRepository.updateEpicHealthcheck(prefixId, true);
            } else if (code == 100) {
                prefixRepository.updateEpicHealthcheck(prefixId, false);
            } else {
                throw new IllegalStateException(
                        "Unexpected response code " + code);
            }

        } catch (WebApplicationException e) {
            if (e.getResponse() != null
                    && e.getResponse().getStatus() == 404) {
                prefixRepository.updateEpicHealthcheck(prefixId, false);
                return;
            }

            LOG.errorf(e, "Failed EPIC_HEALTHCHECK for %s", prefixName);

        } catch (Exception e) {
            LOG.errorf(e, "Failed EPIC_HEALTHCHECK for %s", prefixName);
        }
    }
}