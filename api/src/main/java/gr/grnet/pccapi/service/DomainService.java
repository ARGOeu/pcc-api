package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.domain.DomainDto;
import gr.grnet.pccapi.mapper.DomainMapper;
import gr.grnet.pccapi.repository.DomainRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@AllArgsConstructor
public class DomainService {

  DomainRepository domainRepository;

  Logger logger;


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
