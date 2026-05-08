package gr.grnet.pccapi.repository;

import gr.grnet.connectors.mysql.HRLSConnector;
import gr.grnet.pccapi.entity.Statistics;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

@ApplicationScoped
public class StatisticsRepository implements PanacheRepositoryBase<Statistics, Integer> {

  public int getPIDCountByPrefixID(String prefix) throws SQLException {
    int total = 0;
    String query = "SELECT handles_count FROM prefixes WHERE prefix=?";

    try (Connection connection = HRLSConnector.getHRLSConnector().getConnection();
        PreparedStatement ps = connection.prepareStatement(query)) {
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
    String query = "SELECT resolvable_count FROM prefixes WHERE prefix=?";
    String connectionUrl = null;

    try (Connection connection = HRLSConnector.getHRLSConnector().getConnection();
        PreparedStatement ps = connection.prepareStatement(query)) {
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

  public Statistics getPrefixStatisticsByID(String prefix) throws SQLException {

    String query =
        "SELECT prefix, handles_count, resolvable_count, unresolvable_count, unchecked_count FROM prefixes WHERE prefix =?";

    try (Connection connection = HRLSConnector.getHRLSConnector().getConnection();
        PreparedStatement ps = connection.prepareStatement(query)) {
      ps.setString(1, prefix);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return new Statistics(
              rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5));
        } else {
          throw new IllegalArgumentException(String.format("Prefix %s not found", prefix));
        }
      }
    } catch (SQLException e) {
      throw new SQLException(e);
    }
  }

  public Optional<Statistics> insertPrefixStatistics(
      String prefix, int handleCount, int resolvable, int unresolvable, int unchecked)
      throws SQLException {

    String isExistQuery = "SELECT count(*) from prefixes where prefix =?";
    String insertQuery =
        "INSERT IGNORE INTO prefixes ( prefix,handles_count, resolvable_count, unresolvable_count, unchecked_count)"
            + " VALUES(?,?,?,?,?)";

    try (Connection connection = HRLSConnector.getHRLSConnector().getConnection();
        PreparedStatement ps = connection.prepareStatement(isExistQuery)) {
      ps.setString(1, prefix);

      ResultSet rs = ps.executeQuery();
      int count = 0;
      if (rs.next()) {
        count = rs.getInt(1);
      }
      if (count == 0) {
        try (PreparedStatement ps1 = connection.prepareStatement(insertQuery)) {
          ps1.setString(1, prefix);
          ps1.setInt(2, handleCount);
          ps1.setInt(3, resolvable);
          ps1.setInt(4, unresolvable);
          ps1.setInt(5, unchecked);

          int i = ps1.executeUpdate();
          if (i == 1) {
            return Optional.of(getPrefixStatisticsByID(prefix));
          } else {
            return Optional.empty();
          }

        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      } else {
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Optional<Statistics> updatePrefixStatistics(
      String prefix, int handleCount, int resolvable, int unresolvable, int unchecked)
      throws SQLException {

    String query =
        "UPDATE prefixes SET handles_count =? , resolvable_count =? , unresolvable_count =? , unchecked_count =? "
            + " WHERE  prefix =?";

    try (Connection connection = HRLSConnector.getHRLSConnector().getConnection();
        PreparedStatement ps = connection.prepareStatement(query)) {
      ps.setInt(1, handleCount);
      ps.setInt(2, resolvable);
      ps.setInt(3, unresolvable);
      ps.setInt(4, unchecked);
      ps.setString(5, prefix);
      int i = ps.executeUpdate();
      if (i == 1) {
        return Optional.of(getPrefixStatisticsByID(prefix));
      } else {
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new SQLException(e);
    }
  }

  public boolean getPrefixByID(String prefix) throws SQLException {
    String query = "SELECT COUNT(*) FROM prefixes WHERE prefix =?";

    try (Connection connection = HRLSConnector.getHRLSConnector().getConnection();
        PreparedStatement ps = connection.prepareStatement(query)) {
      ps.setString(1, prefix);
      ResultSet rs = ps.executeQuery();
      int n = 0;
      if (rs.next()) {
        n = rs.getInt(1);
      }
      if (n > 0) {
        return true;
      } else {
        return false;
      }

    } catch (SQLException e) {
      throw new SQLException(e);
    }
  }

  public void executeUpdateResolvablePerPrefixProc() {

    String call = "{ call UpdateResolvableCountForAuxHandles() }";
    try (Connection connection = HRLSConnector.getHRLSConnector().getConnection()) {
      try (CallableStatement stmt = connection.prepareCall(call)) {
        stmt.execute();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public ArrayList<String> getHandlessOfAuxHandles(int limit, int offset) {

    ArrayList<String> handles = new ArrayList<>();

    String query = "SELECT handle FROM aux_handles LIMIT " + limit + " OFFSET " + offset;

    try (Connection connection = HRLSConnector.getHRLSConnector().getConnection();
        PreparedStatement ps = connection.prepareStatement(query)) {

      try (ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
          String handle = rs.getString("handle");
          handles.add(handle);
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    } catch (SQLException e) {
      throw new RuntimeException();
    }
    return handles;
  }

  public void deleteResolvedAuxHandles() {

    String query = "DELETE FROM aux_handles where is_resolved=1";

    try (Connection connection = HRLSConnector.getHRLSConnector().getConnection();
        PreparedStatement ps = connection.prepareStatement(query)) {
      ps.execute();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }
}
