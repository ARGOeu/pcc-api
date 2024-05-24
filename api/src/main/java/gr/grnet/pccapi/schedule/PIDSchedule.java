package gr.grnet.pccapi.schedule;

import gr.grnet.pccapi.repository.StatisticsRepository;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * A job has been defined with a cron-like expression {cron.expr} which is configurable in
 * application.properties. The {@link PIDSchedule PID Scheduler} is executed at {cron.expr} and
 * collects the available Scientific Domains. Subsequently, the Domains either are stored in the
 * database or updated.
 */
@ApplicationScoped
public class PIDSchedule {
  @ConfigProperty(name = "daemon.jar.path")
  String daemonJarPath;

  @ConfigProperty(name = "daemon.log.path")
  String daemonLogPath;

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
    ArrayList<String> handles = statisticsRepository.getHandlessOfAuxHandles();
    resolvePrefixes(handles);
  }

  @Transactional
  private void executePIDResolveProcess(ArrayList<String> handles) {
    File f = new File(daemonLogPath);
    if (!f.exists()) {

      f.getParentFile().mkdirs();
      try {
        f.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    handles.forEach(this::executeCommandJar);
  }

  private void executeCommandJar(String handle) {
    Process proc = null;

    int splitIndex = handle.indexOf("/");
    if (splitIndex > 0) {
      String prefix = handle.substring(0, splitIndex);
      String logPath = "";
      if (Files.exists(Paths.get(daemonLogPath))) {
        logPath = " -l " + daemonLogPath;
      }

      String execCommand =
          "java -jar "
              + daemonJarPath
              + " -e 86400"
              + " -m HEAD "
              + " -N 1"
              + " -s 1"
              + " -p "
              + handle
              + " -v FINE -T 1"
              + " "
              + logPath
              + " -q HANDLE";
      try {
        proc = Runtime.getRuntime().exec(execCommand);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      synchronized (proc) {
        try {
          proc.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        try {
          statisticsRepository.insertPrefixStatistics(prefix, 0, 0, 0, 0);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void resolvePrefixes(ArrayList<String> handles) {
    if (!Files.exists(Paths.get(daemonJarPath))) {
      return;
    }

    CustomCompletableFuture.runAsync(() -> executePIDResolveProcess(handles))
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
}
