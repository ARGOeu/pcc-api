package gr.grnet.pccapi.schedule;

import gr.grnet.pccapi.client.EOSCPortalClient;
import gr.grnet.pccapi.service.DomainService;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class DomainSchedule {

    @Inject
    @RestClient
    EOSCPortalClient eoscPortalClient;

    @Inject
    DomainService domainService;

    @Inject
    Logger LOG;

    void onStart(@Observes StartupEvent ev) {
        execute();
    }

    @Scheduled(cron = "{cron.expr}")
    void cronJobWithExpressionInConfig() {
        execute();
    }

    private void execute(){

        var domains = eoscPortalClient.getByType("SCIENTIFIC_DOMAIN");

        domains
                .thenAccept(response -> {
                    LOG.info("Inserting the EOSC-Portal domains into the database.");
                    domainService.saveEoscPortalDomains(response);
                })
                .exceptionally(ex -> {
            LOG.error("Failed to communicate with EOSC-Portal.", ex);
            return null;
        });
    }
}
