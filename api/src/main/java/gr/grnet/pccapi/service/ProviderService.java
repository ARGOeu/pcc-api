package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.provider.ProviderResponseDTO;
import gr.grnet.pccapi.mapper.ProviderMapper;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.resources.ProviderResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.grnet.endpoint.scanner.runtime.context.RoleEndpointHolder;

import static com.fasterxml.jackson.databind.type.LogicalType.Collection;

@ApplicationScoped
@AllArgsConstructor
public class ProviderService {

  @Inject
  AccessControlService accessControlService;

  @Inject
  ProviderRepository providerRepository;

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
}
