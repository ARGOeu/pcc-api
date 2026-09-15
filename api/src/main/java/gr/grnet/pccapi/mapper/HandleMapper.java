package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.client.handle.HandleClientListResponse;
import gr.grnet.pccapi.client.handle.HandleClientRequest;
import gr.grnet.pccapi.client.handle.HandleClientResponse;
import gr.grnet.pccapi.dto.handle.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
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


    @Mapping(target = "values", expression = "java(mapUpdateValues(request.getValues(), existingHandle.getValues()))")
    HandleClientRequest updateToClientRequest(HandleUpdateRequestDto request, HandleClientResponse existingHandle);

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
    default List<HandleValueResponseDto> mapGetValues(List<HandleClientResponse.Value> values) {

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

    @Named("mapUpdateValues")
    default List<HandleClientRequest.Value> mapUpdateValues(List<HandleTypeValue> requestValues, List<HandleClientResponse.Value> existingValues) {

        List<HandleClientRequest.Value> values = new ArrayList<>();

        var index = 1;

        for (var value : requestValues) {
            values.add(new HandleClientRequest.Value(
                    index++,
                    value.getType(),
                    new HandleClientRequest.Data(
                            "string",
                            value.getValue())));
        }

        var admin = existingValues.stream()
                .filter(value -> "HS_ADMIN".equals(value.getType()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("Handle does not contain an HS_ADMIN value."));

        values.add(new HandleClientRequest.Value(
                admin.getIndex(),
                admin.getType(),
                new HandleClientRequest.Data(
                        admin.getData().getFormat(),
                        admin.getData().getValue())));

        return values;
    }
}