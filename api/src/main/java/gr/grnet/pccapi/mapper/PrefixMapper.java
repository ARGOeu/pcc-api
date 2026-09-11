package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.dto.prefix.PartialPrefixDto;
import gr.grnet.pccapi.dto.prefix.PrefixRequestDto;
import gr.grnet.pccapi.dto.prefix.PrefixResponseDto;
import gr.grnet.pccapi.entity.Prefix;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/** Mapper class for converting between {@link Prefix} and {@link PrefixResponseDto}. */
@Mapper(imports = StringUtils.class)
public interface PrefixMapper {

  PrefixMapper INSTANCE = Mappers.getMapper(PrefixMapper.class);

  String REQUEST_DATE_FORMAT = "yyyy-MM-dd";
  String RESPONSE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /**
   * Maps a prefix to its respective dto.
   *
   * @param prefix entity
   * @return prefix response dto
   */
  @Mapping(target = "serviceId", source = "service.id")
  @Mapping(target = "serviceName", source = "service.name")
  @Mapping(target = "domainId", source = "domain.id")
  @Mapping(target = "domainName", source = "domain.name")
  @Mapping(target = "providerId", source = "provider.id")
  @Mapping(target = "providerName", source = "provider.name")
  @Mapping(target = "lookUpServiceTypeId", source = "lookUpServiceType.id")
  @Mapping(target = "lookUpServiceName", source = "lookUpServiceType.name")
  @Mapping(target = "contractTypeId", source = "contractType.id")
  @Mapping(target = "contractTypeName", source = "contractType.name")
  @Mapping(target = "contractEnd", expression = "java(prefix.contractEnd != null ? convertToString(prefix.contractEnd) : null)")
  PrefixResponseDto prefixToResponseDto(Prefix prefix);

  List<PrefixResponseDto> prefixesToResponseDto(List<Prefix> prefixes);

  @Mapping(target = "name", expression = "java(StringUtils.isNotEmpty(prefixDto.name) ? prefixDto.name : prefix.name)")
  @Mapping(target = "owner", expression = "java(StringUtils.isNotEmpty(prefixDto.owner) ? prefixDto.owner : prefix.owner)")
  @Mapping(target = "usedBy", expression = "java(StringUtils.isNotEmpty(prefixDto.usedBy) ? prefixDto.usedBy : prefix.usedBy)")
  @Mapping(target = "status", expression = "java(prefixDto.status != null ? Integer.parseInt(prefixDto.status) : prefix.status)")
  @Mapping(target = "contactEmail", expression = "java(StringUtils.isNotEmpty(prefixDto.contactEmail) ? prefixDto.contactEmail : prefix.contactEmail)")
  @Mapping(target = "contactName", expression = "java(StringUtils.isNotEmpty(prefixDto.contactName) ? prefixDto.contactName : prefix.contactName)")
  @Mapping(target = "contractEnd", expression = "java(prefixDto.contractEnd != null && StringUtils.isNotEmpty(prefixDto.contractEnd) ? convertToMillis(prefixDto.contractEnd) : prefix.contractEnd)")
  void updatePrefixFromDto(PartialPrefixDto prefixDto, @MappingTarget Prefix prefix);

  @Mapping(target = "contractEnd", expression = "java(prefixRequestDto.contractEnd != null && StringUtils.isNotEmpty(prefixRequestDto.contractEnd) ? convertToMillis(prefixRequestDto.contractEnd) : null)")
  Prefix requestToPrefix(PrefixRequestDto prefixRequestDto);

  @Mapping(target = "contractEnd", expression = "java(prefixRequestDto.contractEnd != null && StringUtils.isNotEmpty(prefixRequestDto.contractEnd) ? convertToMillis(prefixRequestDto.contractEnd) : null)")
  void updateRequestToPrefix(PrefixRequestDto prefixRequestDto, @MappingTarget Prefix prefix);

  default Timestamp convertToMillis(String contractEnd) {
    try {
      SimpleDateFormat formatter = new SimpleDateFormat(REQUEST_DATE_FORMAT);
      formatter.setLenient(false);
      formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

      return new Timestamp(formatter.parse(contractEnd).getTime());
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  default String convertToString(Timestamp timestamp) {
    Date date = new Date(timestamp.getTime());

    SimpleDateFormat formatter = new SimpleDateFormat(RESPONSE_DATE_FORMAT);
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

    return formatter.format(date);
  }
}