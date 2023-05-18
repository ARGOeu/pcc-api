package gr.grnet.pccapi.repository;

import gr.grnet.connectors.mysql.HRLSConnector;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StatisticsRepository {

  HRLSConnector hrlsConnector;

  public int getPIDCountByPrefixID(String prefix) throws SQLException {
    int total = 0;
    String query = "SELECT handles_count FROM prefixes where prefix=?";

    try (PreparedStatement ps = hrlsConnector.getConnection().prepareStatement(query)) {
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

  public int getResolvablePIDCountByPrefixID(String prefix) throws SQLException {

    int total = 0;
    String query =
        "SELECT count(*) FROM handles where LOWER(CONVERT(type using utf8))='url' AND handle like ? AND resolved=1";
    String connectionUrl = null;

    try (PreparedStatement ps = hrlsConnector.getConnection().prepareStatement(query)) {
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
