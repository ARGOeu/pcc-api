package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.provider.PartialProviderRequest;
import gr.grnet.pccapi.dto.provider.ProviderRequestDto;
import gr.grnet.pccapi.dto.provider.ProviderResponseDTO;
import gr.grnet.pccapi.enums.ContactType;
import gr.grnet.pccapi.exception.ConflictException;
import gr.grnet.pccapi.mapper.ProviderMapper;
import gr.grnet.pccapi.repository.ContactRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.resources.ProviderResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import org.grnet.endpoint.scanner.runtime.context.RoleEndpointHolder;
import org.jboss.logging.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@AllArgsConstructor
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
    public List<ProviderResponseDTO> fetchAll() {

        if (accessControlService.isSuperAdmin()) {
            var providers = providerRepository.findAll().list();
            return ProviderMapper.INSTANCE.providersToResponse(providers);
        }

        var providerIds = RoleEndpointHolder.get().stream()
                .flatMap(role -> accessControlService
                        .resolveAccessibleGroupsByName(role.getRoleName(), ProviderResource.PROVIDER.resourceName())
                        .stream())
                .map(Integer::valueOf)
                .collect(Collectors.toSet());

        var providers = providerRepository.fetchProvidersByIds(providerIds);

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

        var contacts = request.getContacts().stream()
                .map(dto -> {
                    var type = ContactType.valueOf(dto.type);

                    return contactRepository
                            .findByNameEmailAndType(
                                    dto.name, dto.email, type)
                            .orElseGet(() ->
                                    ProviderMapper.INSTANCE.contactDtoToEntity(dto));
                })
                .collect(Collectors.toSet());

        provider.setContacts(contacts);
        providerRepository.persist(provider);

        return ProviderMapper.INSTANCE.providerToResponse(provider);
    }
    @Transactional
    public ProviderResponseDTO update(Integer id, PartialProviderRequest request) {

        var provider = providerRepository.findByIdOptional(id)
                .orElseThrow(() ->
                        new NotFoundException("Provider not found"));

       provider.setDescription(request.description);
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

    @Transactional
    public void deleteAll() {
        providerRepository.deleteAll();
    }
    @Transactional
    public void deleteByName(String name) {
        providerRepository.deleteByName(name);
    }

}
