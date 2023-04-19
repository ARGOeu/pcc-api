package gr.grnet.daemons.hrls.pidprobe;

import static java.lang.String.format;
import static java.lang.Thread.sleep;

import gr.grnet.connectors.mysql.HRLSConnector;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "PID Probe",
    description = "A pcc-api PID probre to check and update the status of PIDs.")
public class PIDProbe implements Callable<Integer> {

  @Option(
      names = {"-h", "--help"},
      usageHelp = true,
      description = "Print this help message")
  private boolean helpRequested = false;

  @Option(
      names = {"-T", "--threads-number"},
      paramLabel = "THREADS_NUM",
      description = "Total number of threads to be spawned")
  private static int threads_num = 4;

  @Option(
      names = {"-N", "--pids-total-number"},
      required = true,
      paramLabel = "PIDS_TOTAL_NUM",
      description = "Total number of PIDs to resolve")
  private static int pids_total_num;

  @Option(
      names = {"-s", "--pids-chunk-size"},
      paramLabel = "PIDS_CHUNK_SIZE",
      description = "The size of a PID chunk that a thread resolves")
  private static int pids_chunk_size = 10;

  @Option(
      names = {"-e", "--expiration"},
      paramLabel = "EXPIRATION_SECONDS",
      description = "The expiration time of the probing result in seconds")
  private static int expiration = 600;

  @Option(
      names = {"-o", "--probe-offset"},
      paramLabel = "PROBE_OFFSET",
      description = "The offset at which the handle probing will start")
  private static int probe_offset = 0;

  @Option(
      names = {"-t", "--http-timeout"},
      paramLabel = "HTTP_TIMEOUT",
      description = "The HTTP timeout in us")
  private static int http_timeout = 100;

  @Option(
      names = {"-p", "--prefix"},
      paramLabel = "PREFIX",
      description = "The specific prefix to use for probing")
  private static String prefix = "";

  Logger logger = Logger.getLogger(PIDProbe.class.getName());

  public static void main(String[] args) {
    new CommandLine(new PIDProbe()).execute(args);
  }

  @Override
  public Integer call() throws Exception {
    HRLSConnector hrlsConnector;

    try {
      Handler fileHandler = new FileHandler("/home/ctriant/pcc-api2.log");
      fileHandler.setFormatter(new MyFormatter());
      logger.addHandler(fileHandler);
      HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
      RequestConfig config =
          RequestConfig.custom()
              .setConnectTimeout(Timeout.ofMilliseconds(http_timeout))
              .setConnectionRequestTimeout(Timeout.ofMilliseconds(http_timeout))
              .build();
      CloseableHttpClient httpclient =
          HttpClients.custom()
              .setConnectionManager(poolingConnManager)
              .setConnectionManagerShared(true)
              .setDefaultRequestConfig(config)
              .build();

      {
        hrlsConnector =
            new HRLSConnector(
                format(
                    "jdbc:mysql://%s:%s/%s?serverTimezone=UTC",
                    System.getenv("HRLS_DATABASE_IP"),
                    System.getenv("HRLS_DATABASE_PORT"),
                    System.getenv("HRLS_DATABASE_NAME")),
                System.getenv("HRLS_DATABASE_USERNAME"),
                System.getenv("HRLS_DATABASE_PASSWORD"));
      }

      int iterations = pids_total_num / pids_chunk_size;
      if (pids_total_num % pids_chunk_size != 0) {
        iterations++;
      }
      logger.log(Level.INFO, String.format("Total number of PIDs: %d %n", pids_total_num));
      logger.log(Level.INFO, String.format("Total number of threads: %d %n", threads_num));
      logger.log(Level.INFO, String.format("Size of PID chunks: %d %n", pids_chunk_size));
      logger.log(Level.INFO, String.format("Total chunks calculated: %d %n", iterations));

      logger.log(
          Level.INFO, String.format("Probing started at: %s %n", Timestamp.from(Instant.now())));

      ExecutorService executor = Executors.newFixedThreadPool(threads_num);
      for (int i = 0; i < iterations; i++) {
        Runnable worker =
            new WorkerThread(
                pids_chunk_size,
                (probe_offset * pids_total_num) + (i * pids_chunk_size),
                expiration,
                prefix,
                hrlsConnector,
                httpclient);
        executor.submit(worker);
      }
      executor.shutdown();
      executor.awaitTermination(60000, TimeUnit.SECONDS);
    } catch (Exception e) {
    }
    logger.log(
        Level.INFO, String.format("Probing finished at: %s %n", Timestamp.from(Instant.now())));
    return 1;
  }
}

class WorkerThread implements Runnable {

  private int limit;
  private int offset;
  private int expiration;
  private String prefix;
  private HRLSConnector hrlsConnector;
  private CloseableHttpClient httpclient;

  Logger logger = Logger.getLogger(PIDProbe.class.getName());

  public WorkerThread(
      int limit,
      int offset,
      int expiration,
      String prefix,
      HRLSConnector hrlsConnector,
      CloseableHttpClient httpclient) {
    this.limit = limit;
    this.offset = offset;
    this.expiration = expiration;
    this.prefix = prefix;
    this.hrlsConnector = hrlsConnector;
    this.httpclient = httpclient;
  }

  public String getPID(int limit, int offset, int expiration, String prefix) throws SQLException {
    String handle;
    String prefix_like = "";
    int size = 0;
    String data = "";

    String sq;
    if (prefix == "") {
      sq =
          "SELECT handle, data FROM handles WHERE type='url' AND TIMESTAMPDIFF(SECOND, last_resolved, UTC_TIMESTAMP()) > ? LIMIT ?,?";
    } else {
      prefix_like = prefix + '%';
      sq =
          "SELECT handle, data FROM handles WHERE type='url' AND prefix like ? AND TIMESTAMPDIFF(SECOND, last_resolved, UTC_TIMESTAMP()) > ? LIMIT ?,?";
    }
    String uq = "UPDATE handles SET resolved=?, last_resolved=? WHERE handle=?";
    int code;
    Connection conn = hrlsConnector.getConnection();
    try (PreparedStatement pss = conn.prepareStatement(sq);
        PreparedStatement psu = conn.prepareStatement(uq)) {

      if (prefix == "") {
        pss.setInt(1, expiration);
        pss.setInt(2, offset);
        pss.setInt(3, limit);
      } else {
        pss.setString(1, prefix_like);
        pss.setInt(2, expiration);
        pss.setInt(3, offset);
        pss.setInt(4, limit);
      }
      try (ResultSet rs = pss.executeQuery()) {
        size = 0;
        while (rs.next()) {
          size++;
          handle = rs.getString("handle");
          data = rs.getString("data");
          try {
            code = get(data);
            logger.log(Level.INFO, String.format(data + ":" + code));
            if (code == 200 || code == 302) {
              psu.setBoolean(1, true);
            } else {
              psu.setBoolean(1, false);
            }
            psu.setObject(2, OffsetDateTime.now(ZoneOffset.UTC));
            psu.setString(3, handle);
            psu.addBatch();
          } catch (Exception e) {
            psu.setBoolean(1, false);
            psu.setObject(2, OffsetDateTime.now(ZoneOffset.UTC));
            psu.setString(3, handle);
            psu.addBatch();
            int[] r = psu.executeBatch();
            logger.log(Level.INFO, String.format("Error connecting to: " + data));
          }
        }
        int[] r = psu.executeBatch();
        return null;
      }
    } catch (SQLException e) {
      throw new SQLException(e);
    }
  }

  public int get(String uri) throws Exception {
    HttpHead head = new HttpHead(uri);
    CloseableHttpResponse response = null;
    try {
      response = httpclient.execute(head);
      EntityUtils.consume(response.getEntity());
    } catch (ClientProtocolException ex) {
    } catch (IOException ex) {
    }

    return response.getCode();
  }

  @Override
  public void run() {
    logger.log(
        Level.FINE,
        String.format(
            Thread.currentThread().getName()
                + " Start searching: "
                + this.limit
                + " "
                + this.offset));
    try {
      getPID(this.limit, this.offset, this.expiration, this.prefix);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    processCommand();
    logger.log(
        Level.FINE,
        String.format(
            Thread.currentThread().getName()
                + " Stop searching: "
                + this.limit
                + " "
                + this.offset));
  }

  private void processCommand() {
    try {
      sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
