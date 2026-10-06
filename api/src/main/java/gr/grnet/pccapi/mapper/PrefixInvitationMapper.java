package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.dto.invitation.PrefixInvitationRequest;
import gr.grnet.pccapi.dto.invitation.PrefixInvitationResponse;
import gr.grnet.pccapi.entity.PrefixInvitation;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Mapper(imports = {Timestamp.class, Instant.class})
public interface PrefixInvitationMapper {

    PrefixInvitationMapper INSTANCE = Mappers.getMapper(PrefixInvitationMapper.class);

    @IterableMapping(qualifiedByName = "mapToResponse")
    List<PrefixInvitationResponse> listToDtos(List<PrefixInvitation> prefixInvitations);

    @Named("mapToResponse")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "prefixId", source = "prefix.id")
    @Mapping(target = "prefixName", source = "prefix.name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    PrefixInvitationResponse prefixInvitationToDto(PrefixInvitation prefixInvitation);

    @Named("mapToEntity")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    PrefixInvitation prefixInvitationToEntity(PrefixInvitationRequest request);
}

