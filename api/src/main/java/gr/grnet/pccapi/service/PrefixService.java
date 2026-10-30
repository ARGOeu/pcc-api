package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.pagination.PageResource;
import gr.grnet.pccapi.dto.prefix.PartialPrefixDto;
import gr.grnet.pccapi.dto.prefix.PrefixRequestDto;
import gr.grnet.pccapi.dto.prefix.PrefixResponseDto;
import gr.grnet.pccapi.entity.Codelist;
import gr.grnet.pccapi.entity.Domain;
import gr.grnet.pccapi.entity.PageQuery;
import gr.grnet.pccapi.entity.Prefix;
import gr.grnet.pccapi.entity.Service;
import gr.grnet.pccapi.enums.CodelistCategory;
import gr.grnet.pccapi.enums.resources.PrefixResource;
import gr.grnet.pccapi.exception.ConflictException;
import gr.grnet.pccapi.mapper.PrefixMapper;
import gr.grnet.pccapi.repository.CodelistRepository;
import gr.grnet.pccapi.repository.DomainRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.repository.ServiceRepository;
import gr.grnet.pccapi.enums.resources.ProviderResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.UriInfo;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.endpoint.scanner.runtime.Scope;
import org.grnet.endpoint.scanner.runtime.context.RoleEndpointHolder;
import org.grnet.endpoint.scanner.runtime.entities.RoleEndpoint;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.endpoint.scanner.runtime.entitlements.EntitlementProvider;
import org.jboss.logging.Logger;


@ApplicationScoped
//@AllArgsConstructor
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

    @Inject
    AccessControlService accessControlService;

    @ConfigProperty(name = "api.auth.entitlements.parent-group")
    String parentGroup;

    @Inject
    EntitlementProvider entitlementProvider;

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

    public PageResource<PrefixResponseDto> fetchByProviderByPageAndSize(
            Integer providerId,
            String search,
            String provider,
            String domain,
            String contractType,
            int page,
            int size,
            UriInfo uriInfo) {

        PageQuery<Prefix> prefixes;

        // Super admin can see all prefixes belonging to the requested provider.
        if (accessControlService.isSuperAdmin()) {

            prefixes = prefixRepository.fetchPrefixesByProviderIdAndPage(
                    providerId,
                    search,
                    provider,
                    domain,
                    contractType,
                    page,
                    size
            );

        } else {

            var roles = RoleEndpointHolder.get();

            var userRoles = getRolesFromEntitlements(
                    entitlementProvider.fetchEntitlements()
                            .stream()
                            .map(Entitlement::getRaw)
                            .collect(Collectors.toList())
            );

            // Keep only roles belonging to the current user.
            var matchingRoles = roles.stream()
                    .filter(role -> userRoles.contains(role.getRoleName()))
                    .toList();

            // Resolve the highest applicable scope (ALL takes precedence over MINE).
            var scope = matchingRoles.stream()
                    .map(RoleEndpoint::getScope)
                    .filter(Objects::nonNull)
                    .max(Comparator.comparing(
                            s -> s.equalsIgnoreCase("ALL") ? 1 : 0
                    ))
                    .orElse(null);

            if (scope == null) {
                throw new ForbiddenException(
                        "Scope must be defined for this endpoint!"
                );
            }

            var resolvedScope = Scope.valueOf(scope.toUpperCase());

            if (resolvedScope == Scope.ALL) {

                prefixes = prefixRepository.fetchPrefixesByProviderIdAndPage(
                        providerId,
                        search,
                        provider,
                        domain,
                        contractType,
                        page,
                        size
                );

            } else {

                // Resolve all Provider IDs accessible through MINE roles.
                Set<Integer> accessibleProviderIds = matchingRoles.stream()
                        .filter(role -> Scope.MINE.name()
                                .equalsIgnoreCase(role.getScope()))
                        .flatMap(role ->
                                accessControlService.resolveAccessibleGroupsByName(
                                        role.getRoleName(),
                                        ProviderResource.PROVIDER.resourceName()
                                ).stream()
                        )
                        .map(Integer::valueOf)
                        .collect(Collectors.toSet());

                // Resolve all Prefix IDs accessible through MINE roles.
                Set<Integer> accessiblePrefixIds = matchingRoles.stream()
                        .filter(role -> Scope.MINE.name()
                                .equalsIgnoreCase(role.getScope()))
                        .flatMap(role ->
                                accessControlService.resolveAccessibleGroupsByName(
                                        role.getRoleName(),
                                        PrefixResource.PREFIX.resourceName()
                                ).stream()
                        )
                        .map(Integer::valueOf)
                        .collect(Collectors.toSet());

                prefixes = prefixRepository
                        .fetchPrefixesByProviderIdAndAccessibleResourcesAndPage(
                                providerId,
                                accessibleProviderIds,
                                accessiblePrefixIds,
                                search,
                                provider,
                                domain,
                                contractType,
                                page,
                                size
                        );
            }
        }

        return new PageResource<>(
                prefixes,
                PrefixMapper.INSTANCE.prefixesToResponseDto(prefixes.list()),
                uriInfo
        );
    }
    public PageResource<PrefixResponseDto> fetchAllPrefixesByPageAndSize(String search, String provider, String domain, String contractType, int page, int size, UriInfo uriInfo) {

        var prefixes = prefixRepository.fetchPrefixesByPage(search, provider, domain, contractType, page, size);

        return new PageResource<>(prefixes, PrefixMapper.INSTANCE.prefixesToResponseDto(prefixes.list()), uriInfo);
    }

    /**
     * Returns a Prefix by the given ID
     *
     * @return The stored Prefix has been turned into a response body.
     */
    public PrefixResponseDto fetchById(Integer providerId, Integer prefixId) {

        LOG.infof(
                "Fetching Prefix ID: %s for Provider ID: %s",
                prefixId,
                providerId
        );

        var prefix = prefixRepository
                .findByIdAndProviderId(prefixId, providerId)
                .orElseThrow(() -> new NotFoundException("Prefix not found to this provider"));

        return PrefixMapper.INSTANCE.prefixToResponseDto(prefix);
    }

    @Transactional
    public PrefixResponseDto patchById(int providerId , int prefixId, PartialPrefixDto prefixDto) {

        LOG.info("Partially updating existing prefix . . .");
        var prefix = prefixRepository
                .findByIdAndProviderId(prefixId, providerId)
                .orElseThrow(() -> new NotFoundException("Prefix not found to this provider"));


        // check the uniqueness of the provided name
        if (prefixDto.getName() != null && prefixRepository.existsByName(prefixDto.getName())) {

            var prefixByName = prefixRepository.findByName(prefixDto.getName());

            if (prefixByName != null && prefixByName.id != prefixId) {
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
     * @param prefixId The prefix ID to be deleted
     */
    @Transactional
    public void deleteById(int providerId, int prefixId) {

        var prefix = prefixRepository
                .findByIdAndProviderId(prefixId, providerId)
                .orElseThrow(() ->
                        new NotFoundException("Prefix not found for this provider")
                );

        prefixRepository.delete(prefix);
    }
    /**
     * Full update of a prefix on all attributes
     *
     * @param prefixRequestDto contains the changes that will be applied
     * @param prefixId           the id of prefix to be updated
     * @return PrefixResponseDto
     */
    @Transactional
    public PrefixResponseDto update(PrefixRequestDto prefixRequestDto,int providerId, int prefixId) {

        LOG.info("Full updating a  prefix . . .");
        // check the existence of the provided service
        var prefix = prefixRepository
                .findByIdAndProviderId(prefixId, providerId)
                .orElseThrow(() -> new NotFoundException("Prefix not found to this provider"));

        // check the existence of the provided provider
        var provider =
                providerRepository
                        .findByIdOptional(prefixRequestDto.getProviderId())
                        .orElseThrow(() -> new NotFoundException("Provider not found"));

        // check the uniqueness of the provided name
        if (prefixRepository.existsByName(prefixRequestDto.getName())) {

            var prefixByName = prefixRepository.findByName(prefixRequestDto.getName());

            if (prefixByName != null && prefixByName.id != prefixId) {
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

    @Transactional
    public void deleteAll() {
        prefixRepository.deleteAll();
    }


    private List<String> getRolesFromEntitlements(List<String> rawEntitlements) {

        return rawEntitlements.stream()
                .map(ent -> {

                    int idx = ent.indexOf(parentGroup);

                    if (idx == -1) {
                        return null;
                    }

                    String remaining =
                            ent.substring(idx + parentGroup.length() + 1);

                    return remaining.split(":")[0];
                })
                .filter(Objects::nonNull)
                .toList();
    }

}
