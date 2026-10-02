package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.invitation.PrefixInvitationActionResponse;
import gr.grnet.pccapi.dto.invitation.PrefixInvitationRequest;
import gr.grnet.pccapi.dto.invitation.PrefixInvitationResponse;
import gr.grnet.pccapi.dto.pagination.PageResource;
import gr.grnet.pccapi.enums.InvitationAction;
import gr.grnet.pccapi.enums.InvitationStatus;
import gr.grnet.pccapi.mapper.PrefixInvitationMapper;
import gr.grnet.pccapi.repository.PrefixInvitationRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.endpoint.scanner.runtime.dtos.AssignRoleRequest;
import org.grnet.endpoint.scanner.runtime.services.ResourceAuthorizationService;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static gr.grnet.pccapi.enums.InvitationAction.REVOKE;

@ApplicationScoped
public class PrefixInvitationService {

    @Inject
    PrefixInvitationRepository prefixInvitationRepository;

    @Inject
    PrefixRepository prefixRepository;

    @Inject
    MailerService mailerService;

    @Inject
    GroupManagementService groupManagementService;

    @Inject
    ResourceAuthorizationService resourceAuthorizationService;

    @ConfigProperty(name = "api.ui.url")
    String uiBaseUrl;

    @Transactional
    public PrefixInvitationResponse createInvitation(Integer prefixId, PrefixInvitationRequest request, String createdBy) {

        var existingInvitation = prefixInvitationRepository.findPendingInvitationByPrefixAndEmail(prefixId, request.email);
        var prefix = prefixRepository.findById(prefixId);

        if (prefix == null) {
            throw new WebApplicationException("Creating invitation... Prefix not found.", 404);
        }

        if (existingInvitation.isPresent()) {
            var existing = existingInvitation.get();
            var invitationUrl = uiBaseUrl + "/invitation/" + existing.id;

            mailerService.sendPrefixInvitationEmail(
                    List.of(existing.email),
                    prefix.provider.getName(),
                    prefix.name,
                    getRoleDisplayName(existing.role),
                    invitationUrl);

            return PrefixInvitationMapper.INSTANCE.prefixInvitationToDto(existing);
        }

        var invitation = PrefixInvitationMapper.INSTANCE.prefixInvitationToEntity(request);

        invitation.prefix = prefix;
        invitation.status = InvitationStatus.PENDING;
        invitation.createdBy = createdBy;

        prefixInvitationRepository.persist(invitation);

        var invitationUrl = uiBaseUrl + "/invitation/" + invitation.id;

        mailerService.sendPrefixInvitationEmail(
                List.of(invitation.email),
                prefix.provider.getName(),
                prefix.name,
                getRoleDisplayName(invitation.role),
                invitationUrl);

        return PrefixInvitationMapper.INSTANCE.prefixInvitationToDto(invitation);
    }

    public PageResource<PrefixInvitationResponse> getAllInvitationsByUser(String userEmail, int page, int size, UriInfo uriInfo) {

        var invitations = prefixInvitationRepository.findAllByEmail(userEmail, page, size);

        return new PageResource<>(invitations, PrefixInvitationMapper.INSTANCE.listToDtos(invitations.list()), uriInfo);
    }

    public PrefixInvitationResponse getInvitationById(String id, String userEmail) {

        var invitation = prefixInvitationRepository.findById(id);

        enforceInvitationOwnership(invitation.email, userEmail);

        return PrefixInvitationMapper.INSTANCE.prefixInvitationToDto(invitation);
    }

    public PrefixInvitationResponse getInvitationById(Integer prefixId, String invitationId) {

        var invitation = prefixInvitationRepository.findById(invitationId);

        if (!Objects.equals(invitation.prefix.id, prefixId)) {
            throw new WebApplicationException("Getting invitation... Invitation is not linked to this prefix.", 404);
        }

        return PrefixInvitationMapper.INSTANCE.prefixInvitationToDto(invitation);
    }

    public PageResource<PrefixInvitationResponse> getInvitationsByPageAndSize(String search, String sort, String order, int page, int size, UriInfo uriInfo) {

        var invitations = prefixInvitationRepository.fetchInvitationsByPageAndSize(search, sort, order, page, size);

        return new PageResource<>(invitations, PrefixInvitationMapper.INSTANCE.listToDtos(invitations.list()), uriInfo);
    }

    public PageResource<PrefixInvitationResponse> getInvitationsByPrefixByPageAndSize(String search, String sort, String order, Integer prefixId, int page, int size, UriInfo uriInfo) {

        var invitations = prefixInvitationRepository.fetchInvitationsByPrefixByPageAndSize(search, sort, order, prefixId, page, size);

        return new PageResource<>(invitations, PrefixInvitationMapper.INSTANCE.listToDtos(invitations.list()), uriInfo);
    }

    public PrefixInvitationResponse respondToInvitation(String invitationId, PrefixInvitationActionResponse request, String userEmail, String userUniqueId, String username) {

        var invitation = prefixInvitationRepository.findById(invitationId);

        enforceInvitationOwnership(invitation.email, userEmail);
        enforcePending(invitation.status);

        if (request.action == InvitationAction.ACCEPT) {
            try {
                var addRoleRequest = new AssignRoleRequest();
                addRoleRequest.apiResource = request.apiResource;
                addRoleRequest.resourceId = request.resourceId;
                addRoleRequest.username = username;
                addRoleRequest.role = request.role;

                Log.info("Adding user to provider role.");
                resourceAuthorizationService.assignRoleToUser(addRoleRequest);
            } catch (Exception e) {
                Log.warn("Accepting invitation... Failed to assign role to user.", e);
                throw new WebApplicationException("Accepting invitation... The user could not be assigned the role at the moment.", 503);
            }
        }

        var result = respond(invitationId, request, userEmail, userUniqueId);

        try {
            Log.info("Sending invitation notifications.");
            sendInvitationNotifications(result);
        } catch (Exception e) {
            Log.warn("Notifying for invitation response... Prefix invitation notifications failed.", e);
        }

        return result;
    }

    public PrefixInvitationResponse revokeInvitation(Integer prefixId, String invitationId, String userUniqueId) {
        return revoke(prefixId, invitationId, userUniqueId);
    }

    @Transactional
    public PrefixInvitationResponse respond(String invitationId, PrefixInvitationActionResponse request, String userEmail, String userUniqueId) {

        var invitation = prefixInvitationRepository.findById(invitationId);

        invitation.status = mapToStatus(request.action);
        invitation.respondedAt = Instant.now();
        invitation.respondedBy = userUniqueId;

        return PrefixInvitationMapper.INSTANCE.prefixInvitationToDto(invitation);
    }

    @Transactional
    public PrefixInvitationResponse revoke(Integer prefixId, String invitationId, String userUniqueId) {

        var invitation = prefixInvitationRepository.findById(invitationId);

        if (!Objects.equals(invitation.prefix.id, prefixId)) {
            throw new WebApplicationException("Revoking invitation... Invitation is not linked to this prefix.", 409);
        }

        invitation.status = mapToStatus(REVOKE);
        invitation.respondedAt = Instant.now();
        invitation.respondedBy = userUniqueId;

        return PrefixInvitationMapper.INSTANCE.prefixInvitationToDto(invitation);
    }

    @Transactional
    public void deleteAll() {
        prefixInvitationRepository.deleteAll();
    }

    private void sendInvitationNotifications(PrefixInvitationResponse response) {

        var prefix = prefixRepository.findById(Integer.valueOf(response.prefixId));

        if (prefix == null) {
            Log.warnf("Prefix not found while sending invitation notifications: %s", response.prefixId);
            return;
        }

        var providerId = prefix.provider.getId();
        var providerName = prefix.provider.getName();
        var prefixName = prefix.name;

        if (response.status == InvitationStatus.ACCEPTED) {
            var prefixDetailsUrl = uiBaseUrl + "/providers/" + providerId + "/prefixes/" + response.prefixId;

            try {
                mailerService.sendInvitationAcceptedToInvitee(
                        List.of(response.email),
                        providerName,
                        prefixName,
                        getRoleDisplayName(response.role),
                        prefixDetailsUrl);

            } catch (Exception e) {
                Log.warn("Prefix invitation accepted email to invitee failed: " + response.email, e);
            }
        }

        try {
            var admins = groupManagementService.getProviderMembersByRole(String.valueOf(providerId), "provider_admin");

            if (admins == null) {
                Log.warn("AGM returned null admins list");
                admins = List.of();
            }

            var adminEmails = admins.stream()
                    .map(u -> u.email)
                    .filter(e -> e != null && !e.isBlank())
                    .map(String::trim).distinct().toList();

            if (adminEmails.isEmpty()) {
                Log.warnf("No admin emails found for provider=%s (invitee=%s). No email will be sent.", providerName, response.email);
                return;
            }

            var providerMembersUrl = uiBaseUrl + "/providers/" + providerId + "/members";

            mailerService.sendInvitationResponseToAdmins(
                    adminEmails,
                    providerName,
                    prefixName,
                    response.email,
                    response.role,
                    response.status,
                    providerMembersUrl);

        } catch (Exception e) {
            Log.warn("Prefix invitation response email to provider admins failed.", e);
        }
    }

    private void enforceInvitationOwnership(String invitationEmail, String userEmail) {

        if (userEmail == null || userEmail.isBlank()) {
            throw new BadRequestException("Validating invitation... Authenticated user email is missing.");
        }

        if (invitationEmail == null || invitationEmail.isBlank()) {
            throw new WebApplicationException("Validating invitation... Prefix invitation has no associated email.", 500);
        }

        if (!invitationEmail.equalsIgnoreCase(userEmail)) {
            throw new ForbiddenException("Validating invitation... This invitation does not belong to the authenticated user.");
        }
    }

    private void enforcePending(InvitationStatus status) {

        if (status == null) {
            throw new WebApplicationException("Validating invitation's status... Prefix invitation status is missing.", 409);
        }

        if (status == InvitationStatus.PENDING) {
            return;
        }

        var message = switch (status) {
            case ACCEPTED -> "Prefix invitation already accepted.";
            case REJECTED -> "Prefix invitation already rejected.";
            case REVOKED -> "Prefix invitation has been revoked.";
            default -> "Prefix invitation already responded.";
        };

        throw new WebApplicationException(message, 409);
    }

    private InvitationStatus mapToStatus(InvitationAction action) {
        return switch (action) {
            case ACCEPT -> InvitationStatus.ACCEPTED;
            case REJECT -> InvitationStatus.REJECTED;
            case REVOKE -> InvitationStatus.REVOKED;
        };
    }

    private String getRoleDisplayName(String roleName) {

        return resourceAuthorizationService.getAllRoles()
                .stream()
                .filter(role -> roleName.equals(role.name))
                .findFirst()
                .map(role -> {

                    if (role.attributes == null) { return role.name; }

                    var preferredNames = role.attributes.get("preferred_name");

                    if (preferredNames == null
                            || preferredNames.isEmpty()
                            || preferredNames.get(0) == null
                            || preferredNames.get(0).isBlank()) {

                        return role.name;
                    }

                    return preferredNames.get(0);
                })
                .orElse(roleName);
    }
}