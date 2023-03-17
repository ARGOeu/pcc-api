package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.dto.PartialPrefixDto;
import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.dto.PrefixResponseDto;
import gr.grnet.pccapi.entity.Prefix;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/** Mapper class for converting between {@link Prefix} and {@link PrefixResponseDto} */
@Mapper(imports = StringUtils.class)
public interface PrefixMapper {

  PrefixMapper INSTANCE = Mappers.getMapper(PrefixMapper.class);

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
  PrefixResponseDto prefixToResponseDto(Prefix prefix);

  @Mapping(target = "serviceId", source = "prefix.service.id")
  @Mapping(target = "serviceName", source = "prefix.service.name")
  @Mapping(target = "domainId", source = "prefix.domain.id")
  @Mapping(target = "domainName", source = "prefix.domain.name")
  @Mapping(target = "providerId", source = "prefix.provider.id")
  @Mapping(target = "providerName", source = "prefix.provider.name")
  List<PrefixResponseDto> prefixesToResponseDto(List<Prefix> prefixes);

  PrefixDto prefixToDto(Prefix prefix);

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
          "java(prefixDto.status != null ? Integer.parseInt(prefixDto.status) : prefix.status)")
  @Mapping(
      target = "lookUpServiceType",
      expression =
          "java(prefixDto.lookUpServiceType != null ? prefixDto.lookUpServiceType : prefix.lookUpServiceType)")
  @Mapping(
      target = "contactEmail",
      expression =
          "java(StringUtils.isNotEmpty(prefixDto.contactEmail) ? prefixDto.contactEmail : prefix.contactEmail)")
  @Mapping(
      target = "contactName",
      expression =
          "java(StringUtils.isNotEmpty(prefixDto.contactName) ? prefixDto.contactName : prefix.contactName)")
  void updatePrefixFromDto(PartialPrefixDto prefixDto, @MappingTarget Prefix prefix);
}
