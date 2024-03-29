package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.client.eoscportal.EOSCPortalDomain;
import gr.grnet.pccapi.dto.DomainDto;
import gr.grnet.pccapi.entity.Domain;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DomainMapper {

  DomainMapper INSTANCE = Mappers.getMapper(DomainMapper.class);

  Domain eoscPortalDomainToDomain(EOSCPortalDomain eoscPortalDomains);

  DomainDto domainToDto(Domain domain);

  List<DomainDto> domainsToDto(List<Domain> domains);
}
