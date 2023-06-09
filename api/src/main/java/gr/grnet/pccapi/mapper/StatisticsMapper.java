package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.dto.StatisticsDto;
import gr.grnet.pccapi.entity.Statistics;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StatisticsMapper {

  StatisticsMapper INSTANCE = Mappers.getMapper(StatisticsMapper.class);

  StatisticsDto statisticsToDto(Statistics statistics);
}
