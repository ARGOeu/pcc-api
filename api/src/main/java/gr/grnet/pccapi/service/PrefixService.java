package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.pagination.PageResource;
import gr.grnet.pccapi.dto.prefix.PartialPrefixDto;
import gr.grnet.pccapi.dto.prefix.PrefixRequestDto;
import gr.grnet.pccapi.dto.prefix.PrefixResponseDto;
import gr.grnet.pccapi.entity.Codelist;
import gr.grnet.pccapi.entity.Domain;
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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@AllArgsConstructor
public class PrefixService {

  @Inject
  DomainRepository domainRepository;

  @Inject
  ProviderRepository providerRepository;

  @Inject
  ServiceRepository serviceRepository;

  @Inject
  PrefixRepository prefixRepository;

  @Inject
  CodelistRepository codelistRepository;

  private static final Logger LOG = Logger.getLogger(PrefixService.class);
  /**
   * Creates a new prefix based on the provided arguments, runs validation checks and returns the
   * appropriate response dto
   */
  @Transactional
  public PrefixResponseDto create(PrefixRequestDto prefixRequestDto) {

    LOG.info("Inserting new prefix . . .");

    // check the uniqueness of the provided name
    if (prefixRepository.existsByName(prefixRequestDto.getName())) {
      throw new ConflictException("Prefix name already exists");
    }
    // check the existence of the provided provider
    var provider = providerRepository
            .findByIdOptional(prefixRequestDto.getProviderId())
            .orElseThrow(() -> new NotFoundException("Provider not found"));

    var prefix = PrefixMapper.INSTANCE.requestToPrefix(prefixRequestDto);

    if (prefixRequestDto.contractTypeId != null) {

      var contractType = codelistRepository
              .findByIdAndCategory(prefixRequestDto.contractTypeId, CodelistCategory.CONTRACT_TYPE.getText())
              .orElseThrow(() -> new NotFoundException("Contract Type not found"));

      prefix.setContractType(contractType);
    }

    if (prefixRequestDto.lookUpServiceTypeId != null) {

      var lookUpServiceType = codelistRepository
              .findByIdAndCategory(prefixRequestDto.lookUpServiceTypeId, CodelistCategory.LOOKUP_SERVICE_TYPE.getText())
              .orElseThrow(() -> new NotFoundException("LookUp Service Type not found"));

      prefix.setLookUpServiceType(lookUpServiceType);
    }

    if (prefixRequestDto.getServiceName() != null && !prefixRequestDto.getServiceName().isBlank()) {

      prefix.setService(findOrCreateService(prefixRequestDto.getServiceName()));
    }

    // check the existence of the provided domain
    if (prefixRequestDto.domainId != null) {
      var domain =
          domainRepository
              .findByIdOptional(prefixRequestDto.getDomainId())
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

  public PageResource<PrefixResponseDto> fetchByPageAndSize(String search, String provider, String domain, String contractType, int page, int size, UriInfo uriInfo) {

    var prefixes = prefixRepository.fetchPrefixesByPage(search, provider, domain, contractType, page, size);

    return new PageResource<>(prefixes, PrefixMapper.INSTANCE.prefixesToResponseDto(prefixes.list()), uriInfo);
  }

  /**
   * Returns a Prefix by the given ID
   *
   * @return The stored Prefix has been turned into a response body.
   */
  public PrefixResponseDto fetchById(Integer id) {

    LOG.infof("Fetching the Prefix with ID : %s", id);

    var prefix =
        prefixRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Prefix not found"));

    return PrefixMapper.INSTANCE.prefixToResponseDto(prefix);
  }

  @Transactional
  public PrefixResponseDto patchById(int id, PartialPrefixDto prefixDto) {

    LOG.info("Partially updating existing prefix . . .");
    var prefix =
        prefixRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Prefix not found"));

    // check the uniqueness of the provided name
    if (prefixDto.getName() != null && prefixRepository.existsByName(prefixDto.getName())) {

      var prefixByName = prefixRepository.findByName(prefixDto.getName());

      if (prefixByName != null && prefixByName.id != id) {
        throw new ConflictException("Prefix name already exists");
      }
    }


    PrefixMapper.INSTANCE.updatePrefixFromDto(prefixDto, prefix);
    // check the existence of the provided provider and update entity on success
    if (prefixDto.getProviderId() != null) {
      var provider =
          providerRepository
              .findByIdOptional(prefixDto.getProviderId())
              .orElseThrow(() -> new NotFoundException("Provider not found"));

      prefix.setProvider(provider);
    }

    // check the existence of the provided service and update entity on success

    if (prefixDto.getServiceName() != null && !prefixDto.getServiceName().isBlank()) {

      prefix.setService(findOrCreateService(prefixDto.getServiceName()));
    }

    // check the existence of the provided domain and update entity on success

    if (prefixDto.getDomainId() != null) {
      var domain =
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
                  prefixDto.getLookUpServiceTypeId(),
                  CodelistCategory.LOOKUP_SERVICE_TYPE.getText())
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

    var deleted = prefixRepository.deleteById(id);

    if (!deleted) {
      throw new NotFoundException("Prefix not found");
    }
  }

  /**
   * Full update of a prefix on all attributes
   *
   * @param prefixRequestDto contains the changes that will be applied
   * @param id the id of prefix to be updated
   * @return PrefixResponseDto
   */
  @Transactional
  public PrefixResponseDto update(PrefixRequestDto prefixRequestDto, int id) {

    LOG.info("Full updating a  prefix . . .");
    // check the existence of the provided service
    var prefix =
        prefixRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Prefix not found"));

    // check the existence of the provided provider
    var provider =
        providerRepository
            .findByIdOptional(prefixRequestDto.getProviderId())
            .orElseThrow(() -> new NotFoundException("Provider not found"));

    // check the uniqueness of the provided name
    if (prefixRepository.existsByName(prefixRequestDto.getName())) {

      var prefixByName = prefixRepository.findByName(prefixRequestDto.getName());

      if (prefixByName != null && prefixByName.id != id) {
        throw new ConflictException("Prefix name already exists");
      }
    }

    PrefixMapper.INSTANCE.updateRequestToPrefix(prefixRequestDto, prefix);

    Codelist contractType = null;
    if (prefixRequestDto.contractTypeId != null) {

      contractType =
          codelistRepository
              .findByIdAndCategory(
                  prefixRequestDto.contractTypeId, CodelistCategory.CONTRACT_TYPE.getText())
              .orElseThrow(() -> new NotFoundException("Contract Type not found"));
    }

    prefix.setContractType(contractType);

    Codelist lookUpServiceType = null;
    if (prefixRequestDto.lookUpServiceTypeId != null) {

      lookUpServiceType =
          codelistRepository
              .findByIdAndCategory(
                  prefixRequestDto.lookUpServiceTypeId, CodelistCategory.LOOKUP_SERVICE_TYPE.getText())
              .orElseThrow(() -> new NotFoundException("LookUp Service Type not found"));
    }
    prefix.setLookUpServiceType(lookUpServiceType);

    var service = findOrCreateService(prefixRequestDto.getServiceName());

    prefix.setService(service);

    // check the existence of the provided domain
    Domain domain = null;

    if (prefixRequestDto.domainId != null) {
      domain =
          domainRepository
              .findByIdOptional(prefixRequestDto.getDomainId())
              .orElseThrow(() -> new NotFoundException("Domain not found"));
    }
    prefix.setDomain(domain);

    // update the prefix
    prefix.setProvider(provider);

    return PrefixMapper.INSTANCE.prefixToResponseDto(prefix);
  }

  private Service findOrCreateService(String serviceName) {

    if (serviceName == null || serviceName.isBlank()) {
      return null;
    }

    var normalizedName = serviceName.trim();

    var service = serviceRepository.findByName(normalizedName);

    if (service != null) {
      return service;
    }

    service = new Service();
    service.setName(normalizedName);

    serviceRepository.persist(service);

    return service;
  }
}
