package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.StatisticsDto;
import gr.grnet.pccapi.dto.StatisticsRequestDto;
import gr.grnet.pccapi.entity.Statistics;
import gr.grnet.pccapi.mapper.StatisticsMapper;
import gr.grnet.pccapi.repository.PrefixRepository;
import gr.grnet.pccapi.repository.StatisticsRepository;
import java.sql.SQLException;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class StatisticsService {

  StatisticsRepository statisticsRepository;

  PrefixRepository prefixRepository;

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
      return statisticsRepository.getResolvablePIDCountByPrefixID(prefix);
    } catch (IllegalArgumentException e) {
      throw new NotFoundException(e.getMessage());
    } catch (SQLException e) {
      throw new InternalServerErrorException(e.getMessage());
    }
  }

  public StatisticsDto getPrefixStatisticsByID(String prefix) {
    try {
      var statistics = statisticsRepository.getPrefixStatisticsByID(prefix);
      return StatisticsMapper.INSTANCE.statisticsToDto(statistics);
    } catch (IllegalArgumentException e) {
      throw new NotFoundException(e.getMessage());
    } catch (SQLException e) {
      throw new InternalServerErrorException(e.getMessage());
    }
  }

  public StatisticsDto setPrefixStatistics(String prefix, StatisticsRequestDto statisticsDto) {

    if (!prefixRepository.existsByName(prefix)) {
      throw new BadRequestException("Prefix with name {" + prefix + "} does not exist");
    }

    try {
      Optional<Statistics> statistics;
      if (statisticsRepository.getPrefixByID(prefix)) {

        statistics =
            statisticsRepository.updatePrefixStatistics(
                prefix,
                statisticsDto.handlesCount,
                statisticsDto.resolvableCount,
                statisticsDto.unresolvableCount,
                statisticsDto.uncheckedCount);
      } else {

        statistics =
            statisticsRepository.insertPrefixStatistics(
                prefix,
                statisticsDto.handlesCount,
                statisticsDto.resolvableCount,
                statisticsDto.unresolvableCount,
                statisticsDto.uncheckedCount);
      }

      return StatisticsMapper.INSTANCE.statisticsToDto(
          statistics.orElseThrow(
              () -> new SQLException("Can not handle prefix statistics in hrls database.")));

    } catch (IllegalArgumentException e) {
      throw new NotFoundException(e.getMessage());
    } catch (SQLException e) {
      throw new InternalServerErrorException(e.getMessage());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
