package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.ServiceDto;
import gr.grnet.pccapi.mapper.ServiceMapper;
import gr.grnet.pccapi.repository.ServiceRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer service for the service resource
 */
@ApplicationScoped
public class ServiceService {

    ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

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

    /**
     *
     * Queries the respective {@link ServiceRepository} to retrieve the specified service based on the provided id.
     * It returns a {@link ServiceDto} after converting it using the {@link ServiceMapper}
     */
    public ServiceDto findOneService(Integer id) {
        return ServiceMapper.INSTANCE.serviceToDto(
                serviceRepository.findByIdOptional(id)
                        .orElseThrow(() -> new NotFoundException("Service not found")));
    }
}
