package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.dto.invitation.InvitationRequest;
import gr.grnet.pccapi.dto.invitation.InvitationResponse;
import gr.grnet.pccapi.entity.Invitation;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface InvitationMapper {

    InvitationMapper INSTANCE = Mappers.getMapper(InvitationMapper.class);

    @IterableMapping(qualifiedByName = "mapToResponse")
    List<InvitationResponse> listToDtos(List<Invitation> invitations);

    @Named("mapToResponse")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "apiResource", source = "apiResource")
    @Mapping(target = "resourceId", source = "resourceId")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    InvitationResponse invitationToDto(Invitation invitation);

    @Named("mapToEntity")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "apiResource", source = "apiResource")
    @Mapping(target = "resourceId", source = "resourceId")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "respondedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "respondedBy", ignore = true)
    Invitation invitationToEntity(InvitationRequest request);
}