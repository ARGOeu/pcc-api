package gr.grnet.pccapi.schedule;

import gr.grnet.pccapi.client.eoscportal.EOSCPortalClient;
import gr.grnet.pccapi.service.DomainService;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

/**
 * A job has been defined with a cron-like expression {cron.expr} which is configurable in
 * application.properties. The {@link DomainSchedule Domain Scheduler} is executed at {cron.expr}
 * and collects the available Scientific Domains. Subsequently, the Domains either are stored in the
 * database or updated.
 */
@ApplicationScoped
public class DomainSchedule {

  @Inject @RestClient EOSCPortalClient eoscPortalClient;

  @Inject DomainService domainService;

  @Inject Logger logger;

  void onStart(@Observes StartupEvent ev) {
    execute();
  }

  @Scheduled(cron = "{cron.expr}")
  void cronJobWithExpressionInConfig() {
    execute();
  }

  private void execute() {

    var domains = eoscPortalClient.getByType("SCIENTIFIC_DOMAIN");

    domains
        .thenAccept(
            response -> {
              logger.info("Inserting the EOSC-Portal domains into the database.");
              domainService.saveEoscPortalDomains(response);
            })
        .exceptionally(
            ex -> {
              logger.error("Failed to communicate with EOSC-Portal.", ex);
              return null;
            });
  }
}
