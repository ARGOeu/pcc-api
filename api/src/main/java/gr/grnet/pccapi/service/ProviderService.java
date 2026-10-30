package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.provider.PartialProviderRequest;
import gr.grnet.pccapi.dto.provider.ProviderRequestDto;
import gr.grnet.pccapi.dto.provider.ProviderResponseDTO;
import gr.grnet.pccapi.entity.Provider;
import gr.grnet.pccapi.enums.ContactType;
import gr.grnet.pccapi.enums.resources.PrefixResource;
import gr.grnet.pccapi.exception.ConflictException;
import gr.grnet.pccapi.mapper.ProviderMapper;
import gr.grnet.pccapi.repository.ContactRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.enums.resources.ProviderResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.endpoint.scanner.runtime.Scope;
import org.grnet.endpoint.scanner.runtime.context.RoleEndpointHolder;
import org.grnet.endpoint.scanner.runtime.entities.RoleEndpoint;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.endpoint.scanner.runtime.entitlements.EntitlementProvider;
import org.jboss.logging.Logger;
import gr.grnet.pccapi.utils.ImageUploadUtil;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
//@AllArgsConstructor
public class ProviderService {
    private static final org.jboss.logging.Logger LOG = Logger.getLogger(ProviderService.class);

    @Inject
    AccessControlService accessControlService;

    @Inject
    ProviderRepository providerRepository;
    @Inject
    ContactRepository contactRepository;

    @Inject
    PrefixRepository prefixRepository;

    @Inject
    ImageUploadUtil imageUploadUtil;

    @ConfigProperty(name = "base.upload.logo.dir")
    String baseUploadProvidersLogoDir;

    @ConfigProperty(name = "api.server.url")
    String apiServerUrl;
    @ConfigProperty(name = "api.auth.entitlements.parent-group")
    String parentGroup;

    @Inject
    EntitlementProvider entitlementProvider;


    /**
     * Retrieves the provider with the requested id from the database.
     *
     * @param id The provider id
     * @return The ProviderResponseDTO representation of the requested provider
     */
    public ProviderResponseDTO fetchById(int id) {
        var provider =
                providerRepository
                        .findByIdOptional(id)
                        .orElseThrow(() -> new NotFoundException("Provider not found"));
        // Map the provider retrieved from the database to the equivalent ProviderResponseDTO and return
        return ProviderMapper.INSTANCE.providerToResponse(provider);
    }

    /**
     * Retrieves all the available providers from the database.
     *
     * @return A list of ProviderResponseDTO representations of all available providers
     */
//    public List<ProviderResponseDTO> fetchAll() {
//
//        if (accessControlService.isSuperAdmin()) {
//            var providers = providerRepository.findAll().list();
//            return ProviderMapper.INSTANCE.providersToResponse(providers);
//        }
//
//        var providerIds = RoleEndpointHolder.get().stream()
//                .flatMap(role -> accessControlService
//                        .resolveAccessibleGroupsByName(role.getRoleName(), ProviderResource.PROVIDER.resourceName())
//                        .stream())
//                .map(Integer::valueOf)
//                .collect(Collectors.toSet());
//
//        var providers = providerRepository.fetchProvidersByIds(providerIds);
//
//        return ProviderMapper.INSTANCE.providersToResponse(providers);
//    }
    public List<ProviderResponseDTO> fetchAll() {

        if (accessControlService.isSuperAdmin()) {
            var providers = providerRepository.findAll().list();
            return ProviderMapper.INSTANCE.providersToResponse(providers);
        }

        var userRoles = getRolesFromEntitlements(
                entitlementProvider.fetchEntitlements()
                        .stream()
                        .map(Entitlement::getRaw)
                        .collect(Collectors.toList())
        );

        var matchingRoles = RoleEndpointHolder.get().stream()
                .filter(role -> userRoles.contains(role.getRoleName()))
                .toList();

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

        // ALL scope: retrieve every provider.
        if (resolvedScope == Scope.ALL) {
            var providers = providerRepository.findAll().list();
            return ProviderMapper.INSTANCE.providersToResponse(providers);
        }

        // MINE scope: resolve accessible resources.
        var mineRoles = matchingRoles.stream()
                .filter(role -> Scope.MINE.name().equalsIgnoreCase(role.getScope()))
                .toList();

        // Providers directly accessible through entitlements.
        Set<Integer> providerIds = mineRoles.stream()
                .flatMap(role ->
                        accessControlService.resolveAccessibleGroupsByName(
                                role.getRoleName(),
                                ProviderResource.PROVIDER.resourceName()
                        ).stream()
                )
                .map(Integer::valueOf)
                .collect(Collectors.toSet());

        // Prefixes accessible through entitlements.
        Set<Integer> prefixIds = mineRoles.stream()
                .flatMap(role ->
                        accessControlService.resolveAccessibleGroupsByName(
                                role.getRoleName(),
                                PrefixResource.PREFIX.resourceName()
                        ).stream()
                )
                .map(Integer::valueOf)
                .collect(Collectors.toSet());

        // Retrieve providers either directly accessible or owning accessible prefixes.
        var providers = providerRepository.fetchProvidersByAccessibleResources(
                providerIds,
                prefixIds
        );

        return ProviderMapper.INSTANCE.providersToResponse(providers);
    }
    /**
     * Creates the provider with the requested fields into the database.
     *
     * @return The ProviderResponseDTO representation of the requested provider
     */
    @Transactional
    public ProviderResponseDTO create(ProviderRequestDto request) {

        LOG.info("Inserting new provider . . .");

        if (providerRepository.existsByName(request.getName())) {
            throw new ConflictException("Provider name already exists");
        }

        var provider = ProviderMapper.INSTANCE.providerRequestToEntity(request);

        provider.setLogo(null);

        var contacts = request.getContacts().stream()
                .map(dto -> {
                    var type = ContactType.valueOf(dto.type);

                    return contactRepository
                            .findByNameEmailAndType(dto.name, dto.email, type)
                            .orElseGet(() ->
                                    ProviderMapper.INSTANCE.contactDtoToEntity(dto));
                })
                .collect(Collectors.toSet());

        provider.setContacts(contacts);
        providerRepository.persist(provider);

        handleLogo(request.getLogo(), provider);

        return ProviderMapper.INSTANCE.providerToResponse(provider);
    }



    @Transactional
    public ProviderResponseDTO update(Integer id, PartialProviderRequest request) {

        var provider = providerRepository.findByIdOptional(id)
                .orElseThrow(() ->
                        new NotFoundException("Provider not found"));

        provider.setDescription(request.description);

        handleLogo(request.logo, provider);

        var oldContacts = new HashSet<>(provider.getContacts());
        var newContacts = request.getContacts().stream()
               .map(dto -> {
                   var type = ContactType.valueOf(dto.type);

                   return contactRepository
                            .findByNameEmailAndType(dto.name, dto.email, type)
                            .orElseGet(() ->
                                    ProviderMapper.INSTANCE.contactDtoToEntity(dto));
                })
                .collect(Collectors.toSet());

        // Persist new contacts so their generated IDs are assigned.
        newContacts.stream()
                .filter(contact -> contact.getId() == null)
                .forEach(contactRepository::persist);

        var contactsToDelete = oldContacts.stream()
                .filter(contact -> !newContacts.contains(contact))
                .filter(contact ->
                        !providerRepository.isContactUsedByAnotherProvider(
                                contact.getId(), id))
                .collect(Collectors.toSet());

        provider.setContacts(newContacts);

        contactsToDelete.forEach(contactRepository::delete);

        return ProviderMapper.INSTANCE.providerToResponse(provider);
    }

    @Transactional
    public void delete(Integer id) {

        var provider = providerRepository.findByIdOptional(id)
                .orElseThrow(() ->
                        new NotFoundException("Provider not found"));

        // Do not delete a provider that is connected to a prefix.
        if (prefixRepository.existsByProviderId(id)) {
            throw new ConflictException(
                    "Cannot delete provider because it is associated with one or more prefixes");
        }

        var contacts = new HashSet<>(provider.getContacts());

        // Remove the provider's contact associations.
        provider.setContacts(new HashSet<>());

        // Delete contacts not used by other providers.
        for (var contact : contacts) {
            if (!providerRepository.isContactUsedByAnotherProvider(
                    contact.getId(), id)) {
                contactRepository.delete(contact);
            }
        }

        // Delete the provider.
        providerRepository.delete(provider);
    }

    private void handleLogo(String logo, Provider provider) {

        if (logo == null || logo.isBlank()) {
            provider.setLogo(null);
            return;
        }

        if (logo.startsWith("data:image/")) {

            imageUploadUtil.validateBase64Image(logo);

            var savedPath = imageUploadUtil.saveBase64Image(
                    baseUploadProvidersLogoDir,
                    logo,
                    provider.getId().toString(),
                    "/logos/"
            );

            provider.setLogo(
                    apiServerUrl.replaceAll("/+$", "") + savedPath
            );

        } else {
            provider.setLogo(logo);
        }
    }

    @Transactional
    public void deleteAll() {
        providerRepository.deleteAll();
    }
    @Transactional
    public void deleteByName(String name) {
        providerRepository.deleteByName(name);
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
