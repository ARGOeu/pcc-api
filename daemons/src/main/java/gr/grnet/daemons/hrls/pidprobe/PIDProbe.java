package gr.grnet.daemons.hrls.pidprobe;

import static java.lang.Thread.sleep;

import gr.grnet.connectors.mysql.HRLSConnector;
import java.io.IOException;
import java.net.URI;
import java.sql.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
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

  @Option(
      names = {"-l", "--logfile"},
      paramLabel = "LOGFILE",
      description = "The output logfile")
  private static String logfile = "/tmp/log";

  @Option(
      names = {"-q", "--query"},
      paramLabel = "QUERY TYPE",
      description = "The query type")
  private static String query = "PREFIX";

  Logger logger = Logger.getLogger(PIDProbe.class.getName());

  public static void main(String[] args) {
    new CommandLine(new PIDProbe()).execute(args);
  }

  @Override
  public Integer call() throws Exception {
    HRLSConnector hrlsConnector;

    try {
      Handler fileHandler = new FileHandler(logfile);
      fileHandler.setFormatter(new MyFormatter());
      logger.addHandler(fileHandler);
      logger.setLevel(Level.parse(verbose));
      HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
      RequestConfig config =
          RequestConfig.custom()
              .setConnectTimeout(Timeout.ofMilliseconds(http_timeout))
              .setConnectionRequestTimeout(Timeout.ofMilliseconds(http_timeout))
              .setRedirectsEnabled(true)
              .build();
      logger.log(Level.INFO, String.format("Setting up HTTP client..."));
      CloseableHttpClient httpclient =
          HttpClients.custom()
              .setConnectionManager(poolingConnManager)
              .setConnectionManagerShared(true)
              .setDefaultRequestConfig(config)
              .setRedirectStrategy(new DefaultRedirectStrategy())
              .build();

      {
        logger.log(Level.INFO, String.format("Connecting to DB..."));
      }
      if (query.equals("HANDLE")) {
        logger.log(
            Level.INFO,
            String.format(
                "Probing resolving handle %s started at: %s %n",
                prefix, Timestamp.from(Instant.now())));

        ExecutorService executor = Executors.newFixedThreadPool(threads_num);
        Runnable worker =
            new HandleWorkerThread(expiration, prefix, http_method, request_interval, httpclient);
        executor.submit(worker);
        executor.shutdown();
        executor.awaitTermination(60000, TimeUnit.SECONDS);

      } else {
        int iterations = pids_total_num / pids_chunk_size;
        if (pids_total_num % pids_chunk_size != 0) {
          iterations++;
        }
        logger.log(Level.INFO, String.format("Total number of PIDs: %d %n", pids_total_num));
        logger.log(Level.INFO, String.format("Total number of threads: %d %n", threads_num));
        logger.log(Level.INFO, String.format("Size of PID chunks: %d %n", pids_chunk_size));
        logger.log(Level.INFO, String.format("Total chunks calculated: %d %n", iterations));
        logger.log(
            Level.INFO,
            String.format(
                "Probing prefix %s started at: %s %n", prefix, Timestamp.from(Instant.now())));

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
                  httpclient);
          executor.submit(worker);
        }
        executor.shutdown();
        executor.awaitTermination(60000, TimeUnit.SECONDS);
      }
    } catch (Exception e) {
    }
    logger.log(
        Level.INFO,
        String.format(
            "Probing prefix %s finished at: %s %n", prefix, Timestamp.from(Instant.now())));
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

  public WorkerThread(
      int limit,
      int offset,
      int expiration,
      String prefix,
      String http_method,
      int request_interval,
      CloseableHttpClient httpclient) {
    this.limit = limit;
    this.offset = offset;
    this.expiration = expiration;
    this.prefix = prefix;
    this.http_method = http_method;
    this.request_interval = request_interval;
    this.httpclient = httpclient;
  }

  public String getPID(
      int limit,
      int offset,
      int expiration,
      String prefix,
      String http_method,
      int request_interval)
      throws SQLException {
    String handle;
    String prefix_like = "";
    String data = "";

    String sq;
    if (prefix == "") {
      sq =
          "SELECT handle, data FROM handles WHERE TIMESTAMPDIFF(SECOND, last_resolved, UTC_TIMESTAMP()) > ? AND LOWER(CONVERT(type using utf8))='url' LIMIT ?,?";
    } else {
      prefix_like = prefix + '%';
      sq =
          "SELECT handle, data FROM handles WHERE handle like ? AND LOWER(CONVERT(type using utf8))='url' AND TIMESTAMPDIFF(SECOND, last_resolved, UTC_TIMESTAMP()) > ? LIMIT ?,?";
    }
    String uq = "UPDATE handles SET resolved=?, last_resolved=? WHERE handle=?";
    String dhq = "DELETE from aux_handles where handle=?";

    int code;
    try (Connection conn = HRLSConnector.getHRLSConnector().getConnection();
        PreparedStatement pss = conn.prepareStatement(sq);
        PreparedStatement psu = conn.prepareStatement(uq);
        PreparedStatement pdh = conn.prepareStatement(dhq)) {
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
      logger.log(Level.FINE, pss.toString());
      try (ResultSet rs = pss.executeQuery()) {
        while (rs.next()) {
          handle = rs.getString("handle");
          data = rs.getString("data");

          try {
            if (request_interval > 0) {
              processCommand(request_interval);
            }
            code = http_probe(data, http_method);
            if (code == 200) {
              psu.setBoolean(1, true);
            } else {
              psu.setBoolean(1, false);
            }
            psu.setObject(2, OffsetDateTime.now(ZoneOffset.UTC));
            psu.setString(3, handle);
            psu.addBatch();

            if (code == 200) {
              pdh.setString(1, handle);
              pdh.execute();
            }
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

        return null;
      }
    } catch (SQLException e) {
      throw new SQLException(e);
    }
  }

  public int http_probe(String uri, String method) throws Exception {
    HttpUriRequestBase http_req = new HttpUriRequestBase(method, URI.create(uri));
    logger.log(Level.FINE, http_req.toString());
    CloseableHttpResponse response = null;
    try {
      response = httpclient.execute(http_req);
      logger.log(Level.FINE, response.toString());
    } catch (ClientProtocolException ex) {
      logger.log(Level.FINE, String.format(ex.toString()));
    } catch (IOException ex) {
      logger.log(Level.FINE, String.format(ex.toString()));
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
      getPID(
          this.limit,
          this.offset,
          this.expiration,
          this.prefix,
          this.http_method,
          this.request_interval);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    processCommand(100);
    logger.log(
        Level.FINE,
        String.format(
            Thread.currentThread().getName()
                + " Stop searching: "
                + this.limit
                + " "
                + this.offset));
  }

  private void processCommand(long t) {
    try {
      sleep(t);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}

class HandleWorkerThread implements Runnable {
  private int expiration;
  private String valToResolve;
  private String http_method;
  private int request_interval;
  private HRLSConnector hrlsConnector;
  private CloseableHttpClient httpclient;
  Logger logger = Logger.getLogger(PIDProbe.class.getName());

  public HandleWorkerThread(
      int expiration,
      String valToResolve,
      String http_method,
      int request_interval,
      CloseableHttpClient httpclient) {
    this.expiration = expiration;
    this.valToResolve = valToResolve;
    this.http_method = http_method;
    this.request_interval = request_interval;
    this.httpclient = httpclient;
  }

  public String getPID(
      int expiration, String valToResolve, String http_method, int request_interval)
      throws SQLException {
    logger.log(Level.INFO, " Thread running is : " + Thread.currentThread().getName());
    String sq =
        "SELECT handle, data FROM handles WHERE handle = ? AND LOWER(CONVERT(type using utf8))='url' AND TIMESTAMPDIFF(SECOND, last_resolved, UTC_TIMESTAMP()) > ? ";
    String uq = "UPDATE handles SET resolved=?, last_resolved=? WHERE handle=?";
    String auxhq = "UPDATE aux_handles set is_resolved=1 WHERE ";
    HashMap<String, String> handleRes = new HashMap<>();
    try (Connection conn = HRLSConnector.getHRLSConnector().getConnection();
        PreparedStatement pss = conn.prepareStatement(sq);
        PreparedStatement psu = conn.prepareStatement(uq);
        PreparedStatement psaux = conn.prepareStatement(auxhq)) {

      pss.setString(1, valToResolve);
      pss.setInt(2, expiration);
      logger.log(Level.FINE, pss.toString());

      try (ResultSet rs = pss.executeQuery()) {
        while (rs.next()) {
          String handle = rs.getString("handle");
          String data = rs.getString("data");

          handleRes.put(handle, data);
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }

      ArrayList<String> resolvedHandles = new ArrayList<>();
      ArrayList<String> unresolvedHandles = new ArrayList<>();
      for (Map.Entry<String, String> entry : handleRes.entrySet()) {
        String handleNum = entry.getKey();
        String dataRes = entry.getValue();
        try {
          if (request_interval > 0) {
            processCommand(request_interval);
          }
          int code = http_probe(dataRes, http_method);
          if (code == 200) {
            resolvedHandles.add(handleNum);
          } else {
            unresolvedHandles.add(handleNum);
          }
        } catch (Exception e) {
          unresolvedHandles.add(handleNum);
        }
      }
      for (String resolved : resolvedHandles) {
        psu.setBoolean(1, true);
        psu.setObject(2, OffsetDateTime.now(ZoneOffset.UTC));
        psu.setString(3, resolved);
        logger.log(Level.FINE, "updating handles to resolved=1  for handle : " + resolved);

        try {
          psu.executeUpdate();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      for (String unresolved : unresolvedHandles) {
        psu.setBoolean(1, false);
        psu.setObject(2, OffsetDateTime.now(ZoneOffset.UTC));
        psu.setString(3, unresolved);
        logger.log(Level.FINE, "updating handles to resolved=0  for handle : " + unresolved);

        try {
          psu.executeUpdate();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      String querySubstring = "";

      for (String rhandle : resolvedHandles) {
        if (resolvedHandles.indexOf(rhandle) == 0) {
          querySubstring = " handle='" + rhandle + "'";
        } else {
          querySubstring = querySubstring + " or " + " handle='" + rhandle + "'";
        }
      }
      if (!querySubstring.equals("")) {
        auxhq = auxhq + querySubstring;
        try {
          psaux.executeUpdate(auxhq);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      return null;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public int http_probe(String uri, String method) {
    HttpUriRequestBase http_req = new HttpUriRequestBase(method, URI.create(uri));
    logger.log(Level.FINE, http_req.toString());
    CloseableHttpResponse response = null;
    try {
      response = httpclient.execute(http_req);
      int code = response.getCode();
      logger.log(Level.FINE, "code is in response" + code);
      logger.log(Level.FINE, response.toString());
    } catch (ClientProtocolException ex) {
      logger.log(Level.FINE, String.format(ex.toString()));
    } catch (IOException ex) {
      logger.log(Level.FINE, String.format(ex.toString()));
    }

    return response.getCode();
  }

  @Override
  public void run() {
    logger.log(Level.FINE, String.format(Thread.currentThread().getName() + " Start searching: "));
    try {
      getPID(this.expiration, this.valToResolve, this.http_method, this.request_interval);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    processCommand(100);
    logger.log(Level.FINE, String.format(Thread.currentThread().getName() + " Stop searching: "));
  }

  private void processCommand(long t) {
    try {
      sleep(t);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
