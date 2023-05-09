package gr.grnet.daemons.hrls.pidprobe;

import static java.lang.String.format;
import static java.lang.Thread.sleep;

import gr.grnet.connectors.mysql.HRLSConnector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.sql.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;
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
      names = {"-m", "--http-method"},
      paramLabel = "HTTP_METHOD",
      description = "The HTTP method to use for probing")
  private static String http_method = "HEAD";

  @Option(
          names = {"-w", "--request-interval"},
          paramLabel = "REQUEST_INTERVAL",
          description = "The interval between HTTP request in usecs")
  private static int request_interval = 0;

  @Option(
      names = {"-p", "--prefix"},
      paramLabel = "PREFIX",
      description = "The specific prefix to use for probing")
  private static String prefix = "";

  @Option(
          names = {"-v", "--verbose"},
          paramLabel = "VERBOSE",
          description = "The logging verbose")
  private static String verbose = "INFO";

  Logger logger = Logger.getLogger(PIDProbe.class.getName());

  public static void main(String[] args) {
    new CommandLine(new PIDProbe()).execute(args);
  }

  @Override
  public Integer call() throws Exception {
    HRLSConnector hrlsConnector;

    try {
      Handler fileHandler = new FileHandler("/var/log/pcc-api.log");
      fileHandler.setFormatter(new MyFormatter());
      logger.addHandler(fileHandler);
      logger.setLevel(Level.parse(verbose));
      HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
      RequestConfig config =
          RequestConfig.custom()
              .setConnectTimeout(Timeout.ofMilliseconds(http_timeout))
              .setConnectionRequestTimeout(Timeout.ofMilliseconds(http_timeout))
              .build();
      logger.log(Level.INFO, String.format("Setting up HTTP client..."));
      CloseableHttpClient httpclient =
          HttpClients.custom()
              .setConnectionManager(poolingConnManager)
              .setConnectionManagerShared(true)
              .setDefaultRequestConfig(config)
              .build();

      {
        logger.log(Level.INFO, String.format("Connecting to DB..."));
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

      // Delete the log file where resolvable PID count is appended
      File myObj = new File(prefix + ".log");
      if (myObj.delete()) {
        logger.log(
                Level.INFO, String.format("Deleted already existing: %s", myObj.getName()));
      } else {
        logger.log(
                Level.INFO, String.format("Failed to delete: %s", myObj.getName()));
      }

      logger.log(
          Level.INFO, String.format("Probing prefix %s started at: %s %n", prefix, Timestamp.from(Instant.now())));

      ExecutorService executor = Executors.newFixedThreadPool(threads_num);
      for (int i = 0; i < iterations; i++) {
        Runnable worker =
            new WorkerThread(
                pids_chunk_size,
                (probe_offset * pids_total_num) + (i * pids_chunk_size),
                expiration,
                prefix,
                http_method,
                request_interval,
                hrlsConnector,
                httpclient);
        executor.submit(worker);
      }
      executor.shutdown();
      executor.awaitTermination(60000, TimeUnit.SECONDS);
    } catch (Exception e) {
    }
    logger.log(
        Level.INFO, String.format("Probing prefix %s finished at: %s %n", prefix, Timestamp.from(Instant.now())));
    return 1;
  }
}

class WorkerThread implements Runnable {

  private int limit;
  private int offset;
  private int expiration;
  private String prefix;
  private String http_method;
  private int request_interval;
  private HRLSConnector hrlsConnector;
  private CloseableHttpClient httpclient;

  Logger logger = Logger.getLogger(PIDProbe.class.getName());

  private static final Lock fileLock = new ReentrantLock();

  public WorkerThread(
      int limit,
      int offset,
      int expiration,
      String prefix,
      String http_method,
      int polite,
      HRLSConnector hrlsConnector,
      CloseableHttpClient httpclient) {
    this.limit = limit;
    this.offset = offset;
    this.expiration = expiration;
    this.prefix = prefix;
    this.http_method = http_method;
    this.request_interval = request_interval;
    this.hrlsConnector = hrlsConnector;
    this.httpclient = httpclient;
  }

  public String getPID(int limit, int offset, int expiration, String prefix, String http_method, int request_interval)
      throws SQLException {
    String handle;
    String prefix_like = "";
    int size = 0;
    int count = 0;
    String data = "";

    String sq;
    String pq;
    if (prefix == "") {
      sq =
          "SELECT handle, data FROM handles WHERE TIMESTAMPDIFF(SECOND, last_resolved, UTC_TIMESTAMP()) > ? AND LOWER(CONVERT(type using utf8))='url' LIMIT ?,?";
      pq = "UPDATE prefixes SET resolvable_count=resolvable_count+1 WHERE prefix=?";
    } else {
      prefix_like = prefix + '%';
      sq =
          "SELECT handle, data FROM handles WHERE handle like ? AND LOWER(CONVERT(type using utf8))='url' AND TIMESTAMPDIFF(SECOND, last_resolved, UTC_TIMESTAMP()) > ? LIMIT ?,?";
      pq = "UPDATE prefixes SET resolvable_count=resolvable_count+? WHERE prefix=?";
    }
    String uq = "UPDATE handles SET resolved=?, last_resolved=? WHERE handle=?";
    int code;
    Connection conn = hrlsConnector.getConnection();
    try (PreparedStatement pss = conn.prepareStatement(sq);
        PreparedStatement psu = conn.prepareStatement(uq);
        PreparedStatement psp = conn.prepareStatement(pq)) {

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
        count = 0;
        while (rs.next()) {
          size++;
          handle = rs.getString("handle");
          data = rs.getString("data");
          try {
            if (request_interval > 0) {
              Thread.sleep(request_interval);
            }
            code = http_probe(data, http_method);
            logger.log(Level.FINE, String.format(data + ":" + code));
            if (code == 200) {
              psu.setBoolean(1, true);
              count++;
              if (prefix == "") {
                psp.setString(1, handle.split("/", 2)[0]);
                psp.addBatch();
              }
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
            logger.log(Level.FINE, String.format("Error connecting to: " + data));
          }
        }
        // Execute batch query on handles table
        int[] r = psu.executeBatch();

        appendToFile(prefix+".log", count);
        return null;
      }
    } catch (SQLException e) {
      throw new SQLException(e);
    }
  }

  public int http_probe(String uri, String method) throws Exception {
    HttpUriRequestBase http_req = new HttpUriRequestBase(method, URI.create(uri));
    CloseableHttpResponse response = null;
    try {
      response = httpclient.execute(http_req);
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
      getPID(this.limit, this.offset, this.expiration, this.prefix, this.http_method, this.request_interval);
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

  public void appendToFile(String filePath, int count) {
    try {
      fileLock.lock(); // Acquire the lock

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
        writer.write(String.valueOf(count));
        writer.newLine();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } finally {
      fileLock.unlock(); // Release the lock
    }
  }
}
