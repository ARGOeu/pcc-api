package gr.grnet.pccapi.schedule;

import static java.lang.Thread.sleep;

import gr.grnet.connectors.mysql.HRLSConnector;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

// import org.jboss.logging.Logger;

class HandleWorkerThread implements Runnable {

  private CloseableHttpClient httpclient;
  private List<String> handles;
  private int expiration;
  private int httpRequest;
  private String method;

  // @Inject Logger logger;
  Logger logger = java.util.logging.Logger.getLogger(HandleWorkerThread.class.getName());

  public HandleWorkerThread(
      CloseableHttpClient httpclient,
      List<String> handles,
      int expiration,
      int httpRequest,
      String method) {
    this.httpclient = httpclient;
    this.handles = handles;
    this.expiration = expiration;
    this.httpRequest = httpRequest;
    this.method = method;
  }

  public String getPID(
      CloseableHttpClient httpclient,
      List<String> handles,
      int expiration,
      int httpRequest,
      String method)
      throws SQLException {
    logger.info(" Thread running is : " + Thread.currentThread().getName());
    String sq =
        "SELECT handle, data FROM handles WHERE handle = ? AND "
            + "LOWER(CONVERT(type using utf8))='url' "
            + "AND TIMESTAMPDIFF(SECOND, last_resolved, UTC_TIMESTAMP()) > ? ";
    String uq = "UPDATE handles SET resolved=?, last_resolved=? WHERE handle=?";
    String auxhq = "UPDATE aux_handles set is_resolved=1 WHERE ";
    HashMap<String, String> handleRes = new HashMap<>();
    try (Connection conn = HRLSConnector.getHRLSConnector().getConnection();
        PreparedStatement pss = conn.prepareStatement(sq);
        PreparedStatement psu = conn.prepareStatement(uq);
        PreparedStatement psaux = conn.prepareStatement(auxhq)) {

      for (String handle : handles) {
        pss.setString(1, handle);
        pss.setInt(2, expiration);
        logger.info(pss.toString());

        try (ResultSet rs = pss.executeQuery()) {
          while (rs.next()) {
            String handleVal = rs.getString("handle");
            String data = rs.getString("data");
            handleRes.put(handleVal, data);
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      ArrayList<String> resolvedHandles = new ArrayList<>();
      ArrayList<String> unresolvedHandles = new ArrayList<>();
      for (Map.Entry<String, String> entry : handleRes.entrySet()) {
        String handleNum = entry.getKey();
        String dataRes = entry.getValue();
        try {
          if (httpRequest > 0) {
            processCommand(httpRequest);
          }
          int code = http_probe(httpclient, dataRes, method);

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
        logger.info("updating handles to resolved=1  for handle : " + resolved);
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
        logger.info("updating handles to resolved=0  for handle : " + unresolved);
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

  public int http_probe(CloseableHttpClient httpclient, String uri, String method) {
    HttpUriRequestBase httpReq = new HttpUriRequestBase(method, URI.create(uri));
    logger.info(httpReq.toString());
    CloseableHttpResponse response = null;

    try {
      response = httpclient.execute(httpReq);
      logger.info(response.toString());
    } catch (ClientProtocolException ex) {
      logger.info(String.format(ex.toString()));
    } catch (IOException ex) {
      logger.info(String.format(ex.toString()));
    }

    return response.getCode();
  }

  @Override
  public void run() {
    logger.info(String.format(Thread.currentThread().getName() + " Start searching: "));
    try {
      getPID(this.httpclient, this.handles, this.expiration, this.httpRequest, this.method);
      processCommand(this.httpRequest);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    logger.info(String.format(Thread.currentThread().getName() + " Stop searching: "));
  }

  private void processCommand(long t) {
    try {
      sleep(t);
      sleep(t);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
