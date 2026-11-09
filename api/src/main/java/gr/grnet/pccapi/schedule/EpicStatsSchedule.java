package gr.grnet.pccapi.schedule;

import gr.grnet.pccapi.service.EpicStatsService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EpicStatsSchedule {

    @Inject EpicStatsService epicStatsService;

    @Inject Logger logger;

    @Scheduled(
            cron = "{epic.stats.cron.expr}",
            timeZone = "Europe/Athens",
            concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void updateEpicStats() {

        logger.info("Starting EPIC_STATS synchronization");

        epicStatsService.refreshAllPrefixes();

        logger.info("Finished EPIC_STATS synchronization");
    }
}