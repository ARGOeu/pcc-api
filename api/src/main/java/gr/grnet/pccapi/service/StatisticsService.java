package gr.grnet.pccapi.service;

import gr.grnet.pccapi.repository.StatisticsRepository;
import java.sql.SQLException;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class StatisticsService {

  StatisticsRepository statisticsRepository;

  public int getPIDCountByPrefixID(String prefix) {
    try {
      return statisticsRepository.getPIDCountByPrefixID(prefix);
    } catch (IllegalArgumentException e) {
      throw new NotFoundException(e.getMessage());
    } catch (SQLException e) {
      throw new InternalServerErrorException(e.getMessage());
    }
  }

  public int getResolvablePIDCountByPrefixID(String prefix) {
    try {
      return MySQLConnector.getResolvablePIDCountByPrefixID(prefix);
    } catch (IllegalArgumentException e) {
      throw new NotFoundException(e.getMessage());
    } catch (SQLException e) {
      throw new InternalServerErrorException(e.getMessage());
    }
  }
}
