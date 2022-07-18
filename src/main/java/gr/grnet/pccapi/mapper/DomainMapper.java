package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.dto.DomainDto;
import gr.grnet.pccapi.entity.Domain;
import gr.grnet.pccapi.external.response.EOSCPortalDomain;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DomainMapper {

    DomainMapper INSTANCE = Mappers.getMapper(DomainMapper.class);

    Domain eoscPortalDomainToDomain(EOSCPortalDomain eoscPortalDomains);

    DomainDto domainToDto(Domain domain);

    List<DomainDto> domainsToDto(List<Domain> domains);
}
