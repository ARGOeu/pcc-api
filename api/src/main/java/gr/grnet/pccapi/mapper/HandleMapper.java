package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.client.handle.HandleClientListResponse;
import gr.grnet.pccapi.client.handle.HandleClientResponse;
import gr.grnet.pccapi.dto.handle.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface HandleMapper {

    HandleMapper INSTANCE = Mappers.getMapper(HandleMapper.class);

    @Named("clientToApi")
    HandleListResponseDto toResponseListDto(HandleClientListResponse response);

    @Mapping(target = "handle", source = "response.handle")
    @Mapping(target = "values", source = "request.values", qualifiedByName = "mapCreateValues")
    HandleResponseDto createToResponseDto(HandleClientResponse response, HandleRequestDto request);

    @Mapping(target = "values", source = "values", qualifiedByName = "mapGetValues")
    HandleResponseDto getToResponseDto(HandleClientResponse response);




    @Named("mapCreateValues")
    default List<HandleValueResponseDto> mapCreateValues(List<HandleTypeValue> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .map(value -> new HandleValueResponseDto(
                        null,
                        value.getType(),
                        new HandleDataResponseDto(
                                "string",
                                value.getValue()),
                        null,
                        null))
                .toList();
    }

    @Named("mapGetValues")
    default List<HandleValueResponseDto> mapGetValues(
            List<HandleClientResponse.Value> values) {

        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(value -> !"HS_ADMIN".equals(value.getType()))
                .map(value -> new HandleValueResponseDto(
                        value.getIndex(),
                        value.getType(),
                        new HandleDataResponseDto(
                                value.getData().getFormat(),
                                value.getData().getValue()),
                        value.getTtl(),
                        value.getTimestamp()))
                .toList();
    }
}