package gr.grnet.pccapi.service;

import gr.grnet.connectors.mysql.MySQLConnector;
import java.sql.SQLException;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class StatisticsService {

  public int getPIDCountByPrefixID(String prefix) {
    try {
      return MySQLConnector.getPIDCountByPrefixID(prefix);
    } catch (IllegalArgumentException e) {
      throw new NotFoundException(e.getMessage());
    } catch (SQLException e) {
      throw new InternalServerErrorException(e.getMessage());
    }
  }
}
