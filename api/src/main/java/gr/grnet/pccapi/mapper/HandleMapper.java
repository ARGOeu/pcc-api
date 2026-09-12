package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.client.handle.HandleClientListResponse;
import gr.grnet.pccapi.dto.handle.HandleListResponseDto;
import jdk.jfr.Name;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface HandleMapper {

    HandleMapper INSTANCE = Mappers.getMapper(HandleMapper.class);

    @Name("clientToApi")
    HandleListResponseDto toResponseDto(HandleClientListResponse response);
}