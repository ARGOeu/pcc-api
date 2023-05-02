package gr.grnet.connectors.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLConnector {

  public static int getPIDCountByPrefixID(String prefix) throws SQLException {

    int total = 0;
    String query = "SELECT handles_count FROM prefixes where prefix=?";
    String connectionUrl = null;

    connectionUrl =
        "jdbc:mysql://"
            + System.getenv("HRLS_DATABASE_IP")
            + ":"
            + System.getenv("HRLS_DATABASE_PORT")
            + "/"
            + System.getenv("HRLS_DATABASE_NAME")
            + "?serverTimezone=UTC";

    if (System.getenv("PCC_API_PROFILE").equals("prod")) {
      try {
        Class.forName("com.shaded.mysql.jdbc.Driver");
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    try (Connection conn =
            DriverManager.getConnection(
                connectionUrl,
                System.getenv("HRLS_DATABASE_USERNAME"),
                System.getenv("HRLS_DATABASE_PASSWORD"));
        PreparedStatement ps = conn.prepareStatement(query)) {
      ps.setString(1, prefix);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          total = rs.getInt(1);
          return total;
        } else {
          throw new IllegalArgumentException(String.format("Prefix %s not found", prefix));
        }
      }
    } catch (SQLException e) {
      throw new SQLException(e);
    }
  }

  public static int getResolvablePIDCountByPrefixID(String prefix) throws SQLException {

    int total = 0;
    String query = "SELECT count(*) FROM handles where type='url' AND handle like ? AND resolved=1";
    String connectionUrl = null;

    connectionUrl =
        "jdbc:mysql://"
            + System.getenv("HRLS_DATABASE_IP")
            + ":"
            + System.getenv("HRLS_DATABASE_PORT")
            + "/"
            + System.getenv("HRLS_DATABASE_NAME")
            + "?serverTimezone=UTC";

    if (System.getenv("PCC_API_PROFILE").equals("prod")) {
      try {
        Class.forName("com.shaded.mysql.jdbc.Driver");
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    try (Connection conn =
            DriverManager.getConnection(
                connectionUrl,
                System.getenv("HRLS_DATABASE_USERNAME"),
                System.getenv("HRLS_DATABASE_PASSWORD"));
        PreparedStatement ps = conn.prepareStatement(query)) {
      ps.setString(1, prefix + "%");
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          total = rs.getInt(1);
          return total;
        } else {
          throw new IllegalArgumentException(String.format("Prefix %s not found", prefix));
        }
      }
    } catch (SQLException e) {
      throw new SQLException(e);
    }
  }
}
