package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.invitation.InvitationActionResponse;
import gr.grnet.pccapi.dto.invitation.InvitationRequest;
import gr.grnet.pccapi.dto.invitation.InvitationResponse;
import gr.grnet.pccapi.dto.pagination.PageResource;
import gr.grnet.pccapi.entity.Invitation;
import gr.grnet.pccapi.enums.InvitationAction;
import gr.grnet.pccapi.enums.InvitationStatus;
import gr.grnet.pccapi.mapper.InvitationMapper;
import gr.grnet.pccapi.repository.InvitationRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import gr.grnet.pccapi.repository.ProviderRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.endpoint.scanner.runtime.dtos.AssignRoleRequest;
import org.grnet.endpoint.scanner.runtime.services.ResourceAuthorizationService;

import java.time.Instant;
import java.util.List;

import static gr.grnet.pccapi.enums.InvitationAction.REVOKE;

@ApplicationScoped
public class InvitationService {

    @Inject
    InvitationRepository invitationRepository;

    @Inject
    ProviderRepository providerRepository;

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
    public InvitationResponse createInvitation(InvitationRequest request, String createdBy) {

        validateInvitationRequest(request);

        var existingInvitation = invitationRepository.findPendingInvitation(request.apiResource, request.resourceId, request.email);

        if (existingInvitation.isPresent()) {

            var existing = existingInvitation.get();

            sendInvitationEmail(existing);

            return InvitationMapper.INSTANCE.invitationToDto(existing);
        }

        var invitation = InvitationMapper.INSTANCE.invitationToEntity(request);

        invitation.status = InvitationStatus.PENDING;
        invitation.createdBy = createdBy;

        invitationRepository.persist(invitation);

        sendInvitationEmail(invitation);

        return InvitationMapper.INSTANCE.invitationToDto(invitation);
    }

    public PageResource<InvitationResponse> getAllInvitationsByUser(String userEmail, int page, int size, UriInfo uriInfo) {

        var invitations = invitationRepository.findAllByEmail(userEmail, page, size);

        return new PageResource<>(invitations, InvitationMapper.INSTANCE.listToDtos(invitations.list()), uriInfo);
    }

    public InvitationResponse getInvitationById(String invitationId, String userEmail) {

        var invitation = invitationRepository.findById(invitationId);

        enforceInvitationOwnership(invitation.email, userEmail);

        return InvitationMapper.INSTANCE.invitationToDto(invitation);
    }

    public InvitationResponse getInvitationById(String invitationId) {

        var invitation = invitationRepository.findById(invitationId);

        return InvitationMapper.INSTANCE.invitationToDto(invitation);
    }

    public PageResource<InvitationResponse> getInvitationsByPageAndSize(String apiResource, String resourceId, String search, String sort, String order, int page, int size, UriInfo uriInfo) {

        var invitations = invitationRepository.fetchInvitationsByPageAndSize(apiResource, resourceId, search, sort, order, page, size);

        return new PageResource<>(invitations, InvitationMapper.INSTANCE.listToDtos(invitations.list()), uriInfo);
    }

    public InvitationResponse respondToInvitation(String invitationId, InvitationActionResponse request, String userEmail, String userUniqueId, String username) {

        var invitation = invitationRepository.findById(invitationId);

        enforceInvitationOwnership(invitation.email, userEmail);
        enforcePending(invitation.status);

        if (request.action == InvitationAction.ACCEPT) {

            try {
                assignInvitationRoles(invitation, username);

            } catch (Exception e) {

                Log.warn(
                        "Accepting invitation... Failed to assign role to user.",
                        e
                );

                throw new WebApplicationException(
                        "Accepting invitation... The user could not be assigned the role at the moment.",
                        503
                );
            }
        }

        var result = respond(invitationId, request.action, userUniqueId);

        try {

            Log.info("Sending invitation notifications.");

            sendInvitationNotifications(result);

        } catch (Exception e) {

            Log.warn(
                    "Notifying for invitation response... Invitation notifications failed.",
                    e
            );
        }

        return result;
    }


    private void assignInvitationRoles(Invitation invitation, String username) {

                var roleRequest = new AssignRoleRequest();
                roleRequest.apiResource = invitation.apiResource;
                roleRequest.resourceId = invitation.resourceId;
                roleRequest.username = username;
                roleRequest.role = invitation.role;

                resourceAuthorizationService.assignRoleToUser(roleRequest);
    }

    @Transactional
    public InvitationResponse respond(String invitationId, InvitationAction action, String userUniqueId) {

        var invitation = invitationRepository.findById(invitationId);

        invitation.status = mapToStatus(action);
        invitation.respondedAt = Instant.now();
        invitation.respondedBy = userUniqueId;

        return InvitationMapper.INSTANCE.invitationToDto(invitation);
    }

    public InvitationResponse revokeInvitation(String invitationId, String userUniqueId) {

        return revoke(invitationId, userUniqueId);
    }

    @Transactional
    public InvitationResponse revoke(String invitationId, String userUniqueId) {

        var invitation = invitationRepository.findById(invitationId);

        enforcePending(invitation.status);

        invitation.status = mapToStatus(REVOKE);
        invitation.respondedAt = Instant.now();
        invitation.respondedBy = userUniqueId;

        return InvitationMapper.INSTANCE.invitationToDto(invitation);
    }

    @Transactional
    public void deleteAll() {

        invitationRepository.deleteAll();
    }

    private void validateInvitationRequest(InvitationRequest request) {

        if (request.apiResource == null || request.apiResource.isBlank()) {
            throw new BadRequestException("Creating invitation... API resource is required.");
        }

        if (request.resourceId == null || request.resourceId.isBlank()) {
            throw new BadRequestException("Creating invitation... Resource identifier is required.");
        }

        if (request.role == null || request.role.isBlank()) {
            throw new BadRequestException("Creating invitation... Role is required.");
        }

        var apiResources = resourceAuthorizationService
                .getApiResourcesByPage(0, 100, null)
                .getContent();

        var apiResourceExists = apiResources.stream()
                .anyMatch(resource -> request.apiResource.equals(resource.getResourceName()));

        if (!apiResourceExists) {
            throw new BadRequestException(
                    "Creating invitation... Unsupported API resource: " + request.apiResource
            );
        }

        var roleExists = resourceAuthorizationService
                .getAllRoles()
                .stream()
                .anyMatch(role -> request.role.equals(role.name));

        if (!roleExists) {
            throw new BadRequestException(
                    "Creating invitation... Unsupported role: " + request.role
            );
        }
    }

    private Integer parseResourceId(String resourceId) {

        try {
            return Integer.valueOf(resourceId);

        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    "Resource identifier must be a valid integer."
            );
        }
    }

    private void sendInvitationEmail(Invitation invitation) {

        var invitationUrl = uiBaseUrl + "/invitation/" + invitation.id;

        var target = resolveInvitationTarget(
                invitation.apiResource,
                invitation.resourceId
        );

        mailerService.sendInvitationEmail(
                List.of(invitation.email),
                target.targetType,
                target.targetName,
                target.parentType,
                target.parentName,
                getRoleDisplayName(invitation.role),
                invitationUrl
        );
    }

    private void sendInvitationNotifications(InvitationResponse response) {

        var target = resolveInvitationTarget(
                response.apiResource,
                response.resourceId
        );

        if (response.status == InvitationStatus.ACCEPTED) {

            try {

                mailerService.sendInvitationAcceptedToInvitee(
                        List.of(response.email),
                        target.targetType,
                        target.targetName,
                        target.parentType,
                        target.parentName,
                        getRoleDisplayName(response.role),
                        target.detailsUrl
                );

            } catch (Exception e) {

                Log.warn(
                        "Invitation accepted email to invitee failed: " + response.email,
                        e
                );
            }
        }

        if ("Prefix".equals(response.apiResource)) {

            try {

                var prefix = prefixRepository.findById(
                        parseResourceId(response.resourceId)
                );

                var providerId = prefix.provider.getId();

                var admins = groupManagementService.getProviderMembersByRole(
                        String.valueOf(providerId),
                        "provider_admin"
                );

                if (admins == null) {
                    Log.warn("AGM returned null admins list");
                    admins = List.of();
                }

                var adminEmails = admins.stream()
                        .map(u -> u.email)
                        .filter(e -> e != null && !e.isBlank())
                        .map(String::trim)
                        .distinct()
                        .toList();

                if (adminEmails.isEmpty()) {

                    Log.warnf(
                            "No admin emails found for provider=%s (invitee=%s). No email will be sent.",
                            prefix.provider.getName(),
                            response.email
                    );

                    return;
                }

                var providerMembersUrl = uiBaseUrl
                        + "/providers/"
                        + providerId
                        + "/members";

                mailerService.sendInvitationResponseToAdmins(
                        adminEmails,
                        target.targetType,
                        target.targetName,
                        target.parentType,
                        target.parentName,
                        response.email,
                        getRoleDisplayName(response.role),
                        response.status,
                        providerMembersUrl
                );

            } catch (Exception e) {

                Log.warn(
                        "Invitation response email to provider admins failed.",
                        e
                );
            }
        }
    }

    private InvitationTarget resolveInvitationTarget(String apiResource, String resourceId) {

        switch (apiResource) {

            case "Provider" -> {

                var provider = providerRepository.findById(
                        parseResourceId(resourceId)
                );

                if (provider == null) {
                    throw new NotFoundException("Provider not found.");
                }

                return new InvitationTarget(
                        "Provider",
                        provider.getName(),
                        null,
                        null,
                        uiBaseUrl + "/providers/" + provider.getId()
                );
            }

            case "Prefix" -> {

                var prefix = prefixRepository.findById(
                        parseResourceId(resourceId)
                );

                if (prefix == null) {
                    throw new NotFoundException("Prefix not found.");
                }

                return new InvitationTarget(
                        "Prefix",
                        prefix.name,
                        "Provider",
                        prefix.provider.getName(),
                        uiBaseUrl
                                + "/providers/"
                                + prefix.provider.getId()
                                + "/prefixes/"
                                + prefix.id
                );
            }

            default -> throw new BadRequestException(
                    "Unsupported API resource: " + apiResource
            );
        }
    }

    private void enforceInvitationOwnership(String invitationEmail, String userEmail) {

        if (userEmail == null || userEmail.isBlank()) {
            throw new BadRequestException(
                    "Validating invitation... Authenticated user email is missing."
            );
        }

        if (invitationEmail == null || invitationEmail.isBlank()) {
            throw new WebApplicationException(
                    "Validating invitation... Invitation has no associated email.",
                    500
            );
        }

        if (!invitationEmail.equalsIgnoreCase(userEmail)) {
            throw new ForbiddenException(
                    "Validating invitation... This invitation does not belong to the authenticated user."
            );
        }
    }

    private void enforcePending(InvitationStatus status) {

        if (status == null) {
            throw new WebApplicationException(
                    "Validating invitation's status... Invitation status is missing.",
                    409
            );
        }

        if (status == InvitationStatus.PENDING) {
            return;
        }

        var message = switch (status) {
            case ACCEPTED -> "Invitation already accepted.";
            case REJECTED -> "Invitation already rejected.";
            case REVOKED -> "Invitation has been revoked.";
            default -> "Invitation already responded.";
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

                    if (role.attributes == null) {
                        return role.name;
                    }

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

    private static class InvitationTarget {

        String targetType;
        String targetName;
        String parentType;
        String parentName;
        String detailsUrl;

        InvitationTarget(String targetType, String targetName, String parentType, String parentName, String detailsUrl) {

            this.targetType = targetType;
            this.targetName = targetName;
            this.parentType = parentType;
            this.parentName = parentName;
            this.detailsUrl = detailsUrl;
        }
    }
}