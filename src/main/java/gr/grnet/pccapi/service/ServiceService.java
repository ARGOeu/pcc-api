package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.ServiceDto;
import gr.grnet.pccapi.mapper.ServiceMapper;
import gr.grnet.pccapi.repository.ServiceRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer service for the service resource
 */
@ApplicationScoped
public class ServiceService {

    @Inject
    ServiceRepository serviceRepository;

    /**
     *
     * Queries the respective {@link ServiceRepository} to retrieve all the available services.
     * It returns a list of {@link ServiceDto} after converting them using the {@link ServiceMapper}
     */
    public List<ServiceDto> findAllServices() {
        return serviceRepository
                .findAll()
                .stream()
                .map(ServiceMapper.INSTANCE::serviceToDto)
                .collect(Collectors.toList());
    }
}
