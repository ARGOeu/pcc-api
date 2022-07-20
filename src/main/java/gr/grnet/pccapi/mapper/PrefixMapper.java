package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.dto.ServiceDto;
import gr.grnet.pccapi.entity.Prefix;
import gr.grnet.pccapi.entity.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


/**
 * Mapper class for converting between {@link Prefix} and {@link PrefixDto}
 */
@Mapper
public interface PrefixMapper {

    PrefixMapper INSTANCE = Mappers.getMapper(PrefixMapper.class);

    /**
     * Maps a prefix to its respective dto
     * @param prefix entity
     * @return prefix dto
     */
    @Mapping(target="serviceId", source="service.id")
    @Mapping(target="serviceName", source="service.name")
    @Mapping(target="domainId", source="domain.id")
    @Mapping(target="domainName", source="domain.name")
    @Mapping(target="providerId", source="provider.id")
    @Mapping(target="providerName", source="provider.name")
    PrefixDto prefixToDto(Prefix prefix);
}
