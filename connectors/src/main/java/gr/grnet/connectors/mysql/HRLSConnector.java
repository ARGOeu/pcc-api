package gr.grnet.connectors.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class HRLSConnector {

  private HikariConfig config;
  private HikariDataSource ds;

  private String ip;
  private String port;
  private String name;
  private String url;
  private String user;
  private String password;

  public HRLSConnector(String url, String username, String password) {
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

  public Connection getConnection() throws SQLException {
    return ds.getConnection();
  }
}
