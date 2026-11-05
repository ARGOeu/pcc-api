package gr.grnet.pccapi.schedule;

import gr.grnet.pccapi.service.EpicHealthcheckService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EpicHealthcheckSchedule {

    @Inject
    EpicHealthcheckService epicHealthcheckService;

    @Scheduled(
            cron = "{epic.healthcheck.cron.expr}",
            timeZone = "Europe/Athens",
            concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    void updateEpicHealthcheck() {
        epicHealthcheckService.refreshAllPrefixes();
    }
}
