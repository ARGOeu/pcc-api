package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.dto.ServiceDto;
import gr.grnet.pccapi.entity.Service;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


/**
 * Mapper class for converting between {@link Service} and {@link ServiceDto}
 */
@Mapper
public interface ServiceMapper {

    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);

    /**
     * Maps a service to its respective dto
     * @param service entity
     * @return service dto
     */
    ServiceDto serviceToDto(Service service);
}
