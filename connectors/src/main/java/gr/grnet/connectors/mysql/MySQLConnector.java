package gr.grnet.connectors.mysql;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLConnector {

    public int getPIDCountByPrefixID(){

        int total = 0;
        String sqlSelectAllPersons = "SELECT COUNT(*) FROM nas";
        String connectionUrl = "jdbc:mysql://localhost:3306/test?serverTimezone=UTC";

        try (Connection conn = DriverManager.getConnection(connectionUrl, "username", "password");
             PreparedStatement ps = conn.prepareStatement(sqlSelectAllPersons);
             ResultSet rs = ps.executeQuery()) {

             total = rs.getInt(1);

        } catch (SQLException e) {

        }

        return total;
    }
}
