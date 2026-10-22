package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.statistic.AdminStatisticsResponse;
import gr.grnet.pccapi.dto.statistic.AdminTotalStatistics;
import gr.grnet.pccapi.dto.statistic.StatisticsDto;
import gr.grnet.pccapi.dto.statistic.StatisticsRequestDto;
import gr.grnet.pccapi.entity.Statistics;
import gr.grnet.pccapi.mapper.StatisticsMapper;
import gr.grnet.pccapi.repository.AccountRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.repository.StatisticsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import java.sql.SQLException;
import java.util.Optional;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class StatisticsService {

  @Inject StatisticsRepository statisticsRepository;

  @Inject PrefixRepository prefixRepository;

  @Inject
  ProviderRepository providerRepository;
  @Inject
  AccountRepository accountRepository;

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
              () -> new SQLException("Can not handle prefix statistic in hrls database.")));

    } catch (IllegalArgumentException e) {
      throw new NotFoundException(e.getMessage());
    } catch (SQLException e) {
      throw new InternalServerErrorException(e.getMessage());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public AdminStatisticsResponse getStatistics() {

    AdminTotalStatistics total = new AdminTotalStatistics();

    // Overall counts
    total.totalProvidersNum = providerRepository.count();
    total.totalPrefixesNum = prefixRepository.countPrefixes();
    total.totalAccountsNum = accountRepository.countAccounts();

    // Prefixes grouped by provider
    total.prefixesPerProvider =
            prefixRepository.countPrefixesPerProvider();

    // Accounts grouped by provider
    total.accountsPerProvider =
            accountRepository.countAccountsPerProvider();

    // Wrap the statistics in the requested response
    AdminStatisticsResponse response = new AdminStatisticsResponse();
    response.totalStatistics = total;

    return response;
  }
}
