package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.dto.PartialPrefixDto;
import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.dto.PrefixResponseDto;
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

/** Mapper class for converting between {@link Prefix} and {@link PrefixResponseDto} */
@Mapper(imports = StringUtils.class)
public interface PrefixMapper {

  PrefixMapper INSTANCE = Mappers.getMapper(PrefixMapper.class);
  String format = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /**
   * Maps a prefix to its respective dto
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
  //  @Mapping(target = "lookUpServiceType", source = "lookupServiceType.id")
  //  @Mapping(target = "lookUpServiceName", source = "lookupServiceType.name")
  @Mapping(target = "lookUpServiceTypeId", source = "lookUpServiceType.id")
  @Mapping(target = "lookUpServiceName", source = "lookUpServiceType.name")
  @Mapping(target = "contractTypeId", source = "contractType.id")
  @Mapping(target = "contractTypeName", source = "contractType.name")
  @Mapping(
      target = "contractEnd",
      expression =
          "java(prefix.contractEnd != null && StringUtils.isNotEmpty(convertToString(prefix.contractEnd)) ? "
              + "convertToString(prefix.contractEnd) : null)")
  PrefixResponseDto prefixToResponseDto(Prefix prefix);

  @Mapping(target = "serviceId", source = "prefix.service.id")
  @Mapping(target = "serviceName", source = "prefix.service.name")
  @Mapping(target = "domainId", source = "prefix.domain.id")
  @Mapping(target = "domainName", source = "prefix.domain.name")
  @Mapping(target = "providerId", source = "prefix.provider.id")
  @Mapping(target = "providerName", source = "prefix.provider.name")
  //  @Mapping(target = "lookUpServiceType", source = "prefix.lookupServiceType.id")
  //  @Mapping(target = "lookUpServiceName", source = "prefix.lookupServiceType.name")
  @Mapping(target = "lookUpServiceTypeId", source = "prefix.lookUpServiceType.id")
  @Mapping(target = "lookUpServiceName", source = "prefix.lookUpServiceType.name")
  @Mapping(target = "contractTypeId", source = "prefix.contractType.id")
  @Mapping(target = "contractTypeName", source = "prefix.contractType.name")
  @Mapping(
      target = "contractEnd",
      expression =
          "java(prefix.contractEnd != null && StringUtils.isNotEmpty(prefixDto.contractEnd) ? "
              + "convertToString(prefix.contractEnd) : null)")
  List<PrefixResponseDto> prefixesToResponseDto(List<Prefix> prefixes);

  @Mapping(
      target = "name",
      expression = "java(StringUtils.isNotEmpty(prefixDto.name) ? prefixDto.name : prefix.name)")
  @Mapping(
      target = "owner",
      expression = "java(StringUtils.isNotEmpty(prefixDto.owner) ? prefixDto.owner : prefix.owner)")
  @Mapping(
      target = "usedBy",
      expression =
          "java(StringUtils.isNotEmpty(prefixDto.usedBy) ? prefixDto.usedBy : prefix.usedBy)")
  @Mapping(
      target = "status",
      expression =
          "java(prefixDto.status!=null ? Integer.parseInt(prefixDto.status) : prefix.status)")
  //  @Mapping(
  //      target = "lookUpServiceType",
  //      expression =
  //          "java(prefixDto.lookUpServiceType != null &&
  // StringUtils.isNotEmpty(prefixDto.lookUpServiceType) ? "
  //              + "LookUpServiceType.valueOf(prefixDto.lookUpServiceType) :
  // prefix.lookUpServiceType)")
  @Mapping(
      target = "contactEmail",
      expression =
          "java(StringUtils.isNotEmpty(prefixDto.contactEmail) ? prefixDto.contactEmail : prefix.contactEmail)")
  @Mapping(
      target = "contactName",
      expression =
          "java(StringUtils.isNotEmpty(prefixDto.contactName) ? prefixDto.contactName : prefix.contactName)")
  @Mapping(
      target = "contractEnd",
      expression =
          "java(prefixDto.contractEnd != null && StringUtils.isNotEmpty(prefixDto.contractEnd) ? "
              + "convertToMillis(prefixDto.contractEnd) : prefix.contractEnd)")
  //  @Mapping(
  //      target = "contractType",
  //      expression =
  //          "java(prefixDto.contractType != null && StringUtils.isNotEmpty(prefixDto.contractType)
  // ? "
  //              + "ContractType.valueOf(prefixDto.contractType) : prefix.contractType)")
  void updatePrefixFromDto(PartialPrefixDto prefixDto, @MappingTarget Prefix prefix);

  @Mapping(
      target = "contractEnd",
      expression =
          "java(prefixDto.contractEnd != null && StringUtils.isNotEmpty(prefixDto.contractEnd) ? "
              + "convertToMillis(prefixDto.contractEnd) : null)")
  Prefix requestToPrefix(PrefixDto prefixDto);

  @Mapping(
      target = "contractEnd",
      expression =
          "java(prefixDto.contractEnd != null && StringUtils.isNotEmpty(prefixDto.contractEnd) ? "
              + "convertToMillis(prefixDto.contractEnd) : null)")
  void updateRequestToPrefix(PrefixDto prefixDto, @MappingTarget Prefix prefix);
  //
  //  @Named("validateLookUpServiceType")
  //  default LookUpServiceType validateLookUpServiceType(String lookUpServiceType) {
  //    LookUpServiceType lookUpServiceT;
  //    try {
  //      lookUpServiceT = LookUpServiceType.valueOf(lookUpServiceType);
  //    } catch (IllegalArgumentException e) {
  //      throw new BadRequestException("Invalid lookup_service_type value");
  //    }
  //    return lookUpServiceT;
  //  }
  //
  //  @Named("validateContractType")
  //  default ContractType validateContractType(String contractType) {
  //    ContractType contractTypeT = null;
  //    try {
  //      contractTypeT = ContractType.valueOf(contractType);
  //    } catch (IllegalArgumentException e) {
  //      throw new BadRequestException("Invalid contract type value");
  //    }
  //    return contractTypeT;
  //  }

  default Timestamp convertToMillis(String contractEnd) {
    try {
      SimpleDateFormat formatter = new SimpleDateFormat(format);
      formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
      return new Timestamp(formatter.parse(contractEnd).getTime());
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  default String convertToString(Timestamp timestamp) {

    long millis = timestamp.getTime();
    Date dt = new Date(millis);

    SimpleDateFormat formatter = new SimpleDateFormat(format);
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    return formatter.format(dt);
  }
}
