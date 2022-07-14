package gr.grnet.pccapi.mapper;


import gr.grnet.pccapi.dto.ProviderResponseDTO;
import gr.grnet.pccapi.entity.Provider;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ProviderMapper {

    ProviderMapper INSTANCE = Mappers.getMapper(ProviderMapper.class);

    /**
     * Converts a Provider instance to ProviderResponseDTO
     * @param provider A Provider instance
     * @return The converted ProviderResponseDTO provider
     */
    ProviderResponseDTO providerToResponse(Provider provider);

    /**
     * Converts a list of Provider instances to a list of ProviderResponseDTO instances
     * @param provider A list of Provider instances
     * @return The converted ProviderResponseDTO providers list
     */
    List<ProviderResponseDTO> providersToResponse(List<Provider> providers);

}