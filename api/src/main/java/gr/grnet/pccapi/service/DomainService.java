package gr.grnet.pccapi.service;

import gr.grnet.pccapi.client.eoscportal.EOSCPortalDomain;
import gr.grnet.pccapi.dto.DomainDto;
import gr.grnet.pccapi.mapper.DomainMapper;
import gr.grnet.pccapi.repository.DomainRepository;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@AllArgsConstructor
public class DomainService {

  DomainRepository domainRepository;

  Logger logger;

  /**
   * It accepts the EOSC-Portal domains, turns them into {@link gr.grnet.pccapi.entity.Domain
   * domains} and then stores the transformed Domains into the database. If the EOSC-Portal domain
   * exists in the database, then updates its attributes.
   *
   * @param eoscPortalDomains The {@link EOSCPortalDomain eoscPortalDomains} collected by
   *     EOSC-Portal
   */
  @Transactional
  public void saveEoscPortalDomains(Set<EOSCPortalDomain> eoscPortalDomains) {

    for (EOSCPortalDomain eoscPortalDomain : eoscPortalDomains) {

      var domain = domainRepository.findByDomainId(eoscPortalDomain.domainId);

      var eoscToDomain = DomainMapper.INSTANCE.eoscPortalDomainToDomain(eoscPortalDomain);

      if (domain.isPresent()) {

        if (!domain.get().equals(eoscToDomain)) {

          domainRepository.updateByDomainId(
              eoscToDomain.name, eoscToDomain.description, eoscToDomain.domainId);
        }
      } else {
        domainRepository.persistAndFlush(eoscToDomain);
      }
    }

    logger.info("EOSC-Portal domains have been successfully stored into the database.");
  }

  /**
   * Returns a Domain by the given ID
   *
   * @return The stored Domain has been turned into a response body.
   */
  public DomainDto fetchById(Integer id) {

    logger.infof("Fetching the Domain with ID : %s", id);

    var domain =
        domainRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Domain not found"));

    return DomainMapper.INSTANCE.domainToDto(domain);
  }

  /**
   * Returns the available API Scientific Domains
   *
   * @return The stored Domains has been turned into a response body.
   */
  public List<DomainDto> fetchAll() {

    logger.infof("Fetching all Domains.");

    var domains = domainRepository.findAll().list();

    return DomainMapper.INSTANCE.domainsToDto(domains);
  }
}
