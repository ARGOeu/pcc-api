package gr.grnet.connectors.mysql;

import static java.lang.String.format;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class HRLSConnector {

  private static HRLSConnector instance;

  private HikariConfig config;
  private HikariDataSource ds;

  private String ip;
  private String port;
  private String name;
  private String url;
  private String user;
  private String password;

  private HRLSConnector(String url, String username, String password) {
    config = new HikariConfig();
    config.setJdbcUrl(url);
    config.setUsername(username);
    config.setPassword(password);
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.setMaximumPoolSize(10);
    ds = new HikariDataSource(config);
  }

  public static synchronized HRLSConnector getHRLSConnector() {
    if (instance == null) {
      instance =
          new HRLSConnector(
              format(
                  "jdbc:mysql://%s:%s/%s?serverTimezone=UTC",
                  System.getenv("HRLS_DATABASE_IP"),
                  System.getenv("HRLS_DATABASE_PORT"),
                  System.getenv("HRLS_DATABASE_NAME")),
              System.getenv("HRLS_DATABASE_USERNAME"),
              System.getenv("HRLS_DATABASE_PASSWORD"));
    }
    return instance;
  }

  public Connection getConnection() throws SQLException {
    return ds.getConnection();
  }
}
