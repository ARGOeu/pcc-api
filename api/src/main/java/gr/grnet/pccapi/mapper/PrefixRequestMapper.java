package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.dto.PrefixRequestInitialDto;
import gr.grnet.pccapi.dto.PrefixRequestResponse;
import gr.grnet.pccapi.entity.PrefixRequest;
import gr.grnet.pccapi.enums.PrefixExpectedHandles;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "cdi")
public interface PrefixRequestMapper {

    PrefixRequestMapper INSTANCE =
            Mappers.getMapper(PrefixRequestMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(
            target = "expectedHandles",
            source = "expectedHandles",
            qualifiedByName = "mapStringToExpectedHandles"
    )
    PrefixRequest prefixRequestToEntity(PrefixRequestInitialDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
  //  @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "statusReason", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(
            target = "expectedHandles",
            source = "expectedHandles",
            qualifiedByName = "mapStringToExpectedHandles"
    )
    void updateEntity(
            PrefixRequestInitialDto request,
            @MappingTarget PrefixRequest entity
    );

    @Named("mapStringToExpectedHandles")
    default PrefixExpectedHandles mapStringToExpectedHandles(String value) {
        return value != null && !value.isBlank()
                ? PrefixExpectedHandles.valueOf(value)
                : null;
    }

    @Mapping(target = "providerId", source = "provider.id")
    @Mapping(target = "providerName", source = "provider.name")
    @Mapping(
            target = "expectedHandles",
            source = "expectedHandles",
            qualifiedByName = "mapExpectedHandlesToString"
    )

    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "handledBy", source = "handledBy")
    PrefixRequestResponse prefixRequestToResponse(
            PrefixRequest prefixRequest
    );

    @Named("mapExpectedHandlesToString")
    default String mapExpectedHandlesToString(
            PrefixExpectedHandles expectedHandles
    ) {
        return expectedHandles != null ? expectedHandles.name() : null;
    }

    @Mapping(target = "providerId", source = "provider.id")
    @Mapping(target = "providerName", source = "provider.name")
    @Mapping(target = "updatedAt", source = "updatedAt")

    PrefixRequestResponse toResponse(PrefixRequest entity);
}