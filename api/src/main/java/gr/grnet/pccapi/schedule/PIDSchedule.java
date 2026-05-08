package gr.grnet.pccapi.schedule;

import gr.grnet.pccapi.repository.StatisticsRepository;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.jboss.logging.Logger;

/**
 * A job has been defined with a cron-like expression {cron.expr} which is configurable in
 * application.properties. The {@link PIDSchedule PID Scheduler} is executed at {cron.expr} and
 * collects the available Scientific Domains. Subsequently, the Domains either are stored in the
 * database or updated.
 */
@ApplicationScoped
public class PIDSchedule {
  @Inject StatisticsRepository statisticsRepository;

  @Inject Logger logger;

  void onStart(@Observes StartupEvent ev) {
    execute();
  }

  @Scheduled(cron = "{daemon.cron.expr}")
  void cronJobWithExpressionInConfig() {
    execute();
  }

  private void execute() {
    logger.info("executing PIDSchedule for date : " + Calendar.getInstance().getTime());

    int limit = 100;
    int offset = 0;

    while (true) {

      var handles = statisticsRepository.getHandlessOfAuxHandles(limit, offset);

      if (handles.isEmpty()) {
        break;
      }

      resolvePrefixes(handles);

      offset += limit;
    }
  }

  private void executeScheduler(List<String> handles) {

    executeHandlePIDWorker(handles);
    for (String handle : handles) {
      int splitIndex = handle.indexOf("/");
      if (splitIndex > 0) {
        String prefix = handle.substring(0, splitIndex);
        try {
          statisticsRepository.insertPrefixStatistics(prefix, 0, 0, 0, 0);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void resolvePrefixes(ArrayList<String> handles) {

    CustomCompletableFuture.runAsync(() -> executeScheduler(handles))
        .thenRun(this::callProcedureAndDeleteResolvedHandlesFromAuxHandles);
  }

  private void callProcedureAndDeleteResolvedHandlesFromAuxHandles() {
    statisticsRepository.executeUpdateResolvablePerPrefixProc();
    statisticsRepository.deleteResolvedAuxHandles();
  }

  public static class CustomCompletableFuture<T> extends CompletableFuture<T> {
    static final Executor EXEC = Executors.newCachedThreadPool();

    @Override
    public Executor defaultExecutor() {
      return EXEC;
    }

    @Override
    public <U> CompletableFuture<U> newIncompleteFuture() {
      return new CustomCompletableFuture<>();
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
      return supplyAsync(
          () -> {
            runnable.run();
            return null;
          });
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
      return new CompletableFuture<U>().completeAsync(supplier);
    }
  }

  public void executeHandlePIDWorker(List<String> handles) {
    HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
    RequestConfig config =
        RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(100))
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(100))
            .setRedirectsEnabled(true)
            .build();
    logger.info(String.format("Setting up HTTP client..."));
    CloseableHttpClient httpclient =
        HttpClients.custom()
            .setConnectionManager(poolingConnManager)
            .setConnectionManagerShared(true)
            .setDefaultRequestConfig(config)
            .setRedirectStrategy(new DefaultRedirectStrategy())
            .build();

    try {
      ExecutorService executor = Executors.newFixedThreadPool(1);
      Runnable worker = new HandleWorkerThread(httpclient, handles, 1, 0, "HEAD");
      executor.submit(worker);
      executor.shutdown();
      executor.awaitTermination(60000, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
