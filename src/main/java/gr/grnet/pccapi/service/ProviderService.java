package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.ProviderResponseDTO;
import gr.grnet.pccapi.mapper.ProviderMapper;
import gr.grnet.pccapi.repository.ProviderRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProviderService {

    @Inject
    ProviderRepository providerRepository;

    /**
     * Retrieves the provider with the requested id from the database.
     *
     * @param id The provider id
     * @return The ProviderResponseDTO representation of the requested provider
     */
    public ProviderResponseDTO get(int id) {
        var provider = providerRepository.findById(id);
        // Map the provider retrieved from the database to the equivalent ProviderResponseDTO and return
        return ProviderMapper.INSTANCE.providerToResponse(provider);
    }

    /**
     * Retrieves all the available providers from the database.
     *
     * @return A list of ProviderResponseDTO representations of all available providers
     */
    public List<ProviderResponseDTO> getAll() {
        var providers = providerRepository.findAll().list();
        // Map the providers retrieved from the database to the equivalent ProviderResponseDTO list and return
        return ProviderMapper.INSTANCE.providersToResponse(providers);
    }
}
