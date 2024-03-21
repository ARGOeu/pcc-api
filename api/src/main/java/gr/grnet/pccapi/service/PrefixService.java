package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.PageResource;
import gr.grnet.pccapi.dto.PartialPrefixDto;
import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.dto.PrefixResponseDto;
import gr.grnet.pccapi.entity.Codelist;
import gr.grnet.pccapi.entity.Domain;
import gr.grnet.pccapi.entity.Prefix;
import gr.grnet.pccapi.entity.Provider;
import gr.grnet.pccapi.entity.Service;
import gr.grnet.pccapi.enums.CodelistCategory;
import gr.grnet.pccapi.exception.ConflictException;
import gr.grnet.pccapi.mapper.PrefixMapper;
import gr.grnet.pccapi.repository.CodelistRepository;
import gr.grnet.pccapi.repository.DomainRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.repository.ServiceRepository;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@AllArgsConstructor
public class PrefixService {

  DomainRepository domainRepository;
  ProviderRepository providerRepository;
  ServiceRepository serviceRepository;
  PrefixRepository prefixRepository;
  CodelistRepository codelistRepository;
  Logger logger;

  /**
   * Creates a new prefix based on the provided arguments, runs validation checks and returns the
   * appropriate response dto
   */
  @Transactional
  public PrefixResponseDto create(PrefixDto prefixDto) {

    logger.info("Inserting new prefix . . .");

    // check the uniqueness of the provided name
    if (prefixRepository.existsByName(prefixDto.getName())) {
      throw new ConflictException("Prefix name already exists");
    }
    // check the existence of the provided provider
    Provider provider =
        providerRepository
            .findByIdOptional(prefixDto.getProviderId())
            .orElseThrow(() -> new NotFoundException("Provider not found"));

    Prefix prefix = PrefixMapper.INSTANCE.requestToPrefix(prefixDto);

    if (prefixDto.contractTypeId != null) {

      var contractType =
          codelistRepository
              .findByIdAndCategory(
                  prefixDto.contractTypeId, CodelistCategory.CONTRACT_TYPE.getText())
              .orElseThrow(() -> new NotFoundException("Contract Type not found"));

      prefix.setContractType(contractType);
    }
    System.out.println("look up service id: " + prefixDto.lookUpServiceTypeId);
    if (prefixDto.lookUpServiceTypeId != null) {

      var lookUpServiceType =
          codelistRepository
              .findByIdAndCategory(
                  prefixDto.lookUpServiceTypeId, CodelistCategory.LOOKUP_SERVICE_TYPE.getText())
              .orElseThrow(() -> new NotFoundException("LookUp Service Type not found"));

      prefix.setLookUpServiceType(lookUpServiceType);
    }

    if (prefixDto.serviceId != null) {
      // check the existence of the provided service
      Service service =
          serviceRepository
              .findByIdOptional(prefixDto.getServiceId())
              .orElseThrow(() -> new NotFoundException("Service not found"));
      prefix.setService(service);
    }
    // check the existence of the provided domain
    if (prefixDto.domainId != null) {
      Domain domain =
          domainRepository
              .findByIdOptional(prefixDto.getDomainId())
              .orElseThrow(() -> new NotFoundException("Domain not found"));
      prefix.setDomain(domain);
    }
    prefix.setProvider(provider);
    prefixRepository.persist(prefix);
    return PrefixMapper.INSTANCE.prefixToResponseDto(prefix);
  }

  public List<PrefixResponseDto> fetchAll() {

    var prefixes = prefixRepository.findAll().list();
    // Map the prefixes retrieved from the database to the equivalent prefixDTO list and return
    return PrefixMapper.INSTANCE.prefixesToResponseDto(prefixes);
  }

  public PageResource<PrefixResponseDto> fetchByPageAndSize(int page, int size, UriInfo uriInfo) {

    var prefixes = prefixRepository.fetchPrefixesByPage(page, size);
    return new PageResource<>(
        prefixes, PrefixMapper.INSTANCE.prefixesToResponseDto(prefixes.list()), uriInfo);
  }

  /**
   * Returns a Prefix by the given ID
   *
   * @return The stored Prefix has been turned into a response body.
   */
  public PrefixResponseDto fetchById(Integer id) {

    logger.infof("Fetching the Prefix with ID : %s", id);

    var prefix =
        prefixRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Prefix not found"));

    return PrefixMapper.INSTANCE.prefixToResponseDto(prefix);
  }

  @Transactional
  public PrefixResponseDto patchById(int id, PartialPrefixDto prefixDto) {

    logger.info("Partially updating existing prefix . . .");
    Prefix prefix =
        prefixRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Prefix not found"));

    // check the uniqueness of the provided name
    if (prefixRepository.existsByName(prefixDto.getName())) {
      Prefix prefixByName = prefixRepository.findByName(prefixDto.getName());
      if (prefixByName != null && prefixByName.id != id) {
        throw new ConflictException("Prefix name already exists");
      }
    }

    if (prefixDto.status != null) {
      prefix.status = Integer.valueOf(prefixDto.status);
    }

    PrefixMapper.INSTANCE.updatePrefixFromDto(prefixDto, prefix);
    // check the existence of the provided provider and update entity on success
    if (prefixDto.getProviderId() != null) {
      Provider provider =
          providerRepository
              .findByIdOptional(prefixDto.getProviderId())
              .orElseThrow(() -> new NotFoundException("Provider not found"));
      prefix.setProvider(provider);
    }

    // check the existence of the provided service and update entity on success

    if (prefixDto.getServiceId() != null) {
      Service service =
          serviceRepository
              .findByIdOptional(prefixDto.getServiceId())
              .orElseThrow(() -> new NotFoundException("Service not found"));

      prefix.setService(service);
    }

    // check the existence of the provided domain and update entity on success

    if (prefixDto.getDomainId() != null) {
      Domain domain =
          domainRepository
              .findByIdOptional(prefixDto.getDomainId())
              .orElseThrow(() -> new NotFoundException("Domain not found"));
      prefix.setDomain(domain);
    }
    if (prefixDto.getContractTypeId() != null) {
      var contractType =
          codelistRepository
              .findByIdAndCategory(
                  prefixDto.contractTypeId, CodelistCategory.CONTRACT_TYPE.getText())
              .orElseThrow(() -> new NotFoundException("Contract Type not found"));
      prefix.setContractType(contractType);
    }
    if (prefixDto.getLookUpServiceTypeId() != null) {
      var lookUpService =
          codelistRepository
              .findByIdAndCategory(
                  prefixDto.getLookUpServiceTypeId(), CodelistCategory.LOOKUP_SERVICE_TYPE.getText())
              .orElseThrow(() -> new NotFoundException("LookUp Service Type  not found"));
      prefix.setLookUpServiceType(lookUpService);
    }
    return PrefixMapper.INSTANCE.prefixToResponseDto(prefix);
  }

  /**
   * This method delegates a prefix deletion query to {@link PrefixRepository prefixRepository}.
   *
   * @param id The prefix ID to be deleted
   */
  @Transactional
  public void deleteById(Integer id) {
    boolean deleted = prefixRepository.deleteById(id);
    if (!deleted) {
      throw new NotFoundException("Prefix not found");
    }
  }

  /**
   * Full update of a prefix on all attributes
   *
   * @param prefixDto contains the changes that will be applied
   * @param id the id of prefix to be updated
   * @return PrefixResponseDto
   */
  @Transactional
  public PrefixResponseDto update(PrefixDto prefixDto, int id) {

    logger.info("Full updating a  prefix . . .");
    // check the existence of the provided service
    Prefix prefix =
        prefixRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Prefix not found"));

    // check the existence of the provided provider
    Provider provider =
        providerRepository
            .findByIdOptional(prefixDto.getProviderId())
            .orElseThrow(() -> new NotFoundException("Provider not found"));

    // check the uniqueness of the provided name
    if (prefixRepository.existsByName(prefixDto.getName())) {
      Prefix prefixByName = prefixRepository.findByName(prefixDto.getName());
      if (prefixByName != null && prefixByName.id != id) {
        throw new ConflictException("Prefix name already exists");
      }
    }

    PrefixMapper.INSTANCE.updateRequestToPrefix(prefixDto, prefix);

    Codelist contractType = null;
    if (prefixDto.contractTypeId != null) {

      contractType =
          codelistRepository
              .findByIdAndCategory(
                  prefixDto.contractTypeId, CodelistCategory.CONTRACT_TYPE.getText())
              .orElseThrow(() -> new NotFoundException("Contract Type not found"));
    }

    prefix.setContractType(contractType);

    Codelist lookUpServiceType = null;
    if (prefixDto.lookUpServiceTypeId != null) {

      lookUpServiceType =
          codelistRepository
              .findByIdAndCategory(
                  prefixDto.lookUpServiceTypeId, CodelistCategory.LOOKUP_SERVICE_TYPE.getText())
              .orElseThrow(() -> new NotFoundException("LookUp Service Type not found"));
    }
    prefix.setLookUpServiceType(lookUpServiceType);

    Service service = null;
    if (prefixDto.serviceId != null) {
      service =
          serviceRepository
              .findByIdOptional(prefixDto.getServiceId())
              .orElseThrow(() -> new NotFoundException("Service not found"));
    }
    prefix.setService(service);

    // check the existence of the provided domain
    Domain domain = null;

    if (prefixDto.domainId != null) {
      domain =
          domainRepository
              .findByIdOptional(prefixDto.getDomainId())
              .orElseThrow(() -> new NotFoundException("Domain not found"));
    }
    prefix.setDomain(domain);

    // update the prefix
    prefix.setProvider(provider);

    return PrefixMapper.INSTANCE.prefixToResponseDto(prefix);
  }
}
