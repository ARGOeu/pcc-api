package gr.grnet.pccapi.service;

import gr.grnet.pccapi.entity.Provider;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.resources.ProviderResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.AuthGroupManagement;
import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.response.GroupMembersResponse;
import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.response.GroupUserResponse;
import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.response.PartialGroup;
import org.grnet.endpoint.scanner.runtime.endpoints.PageResource;
import org.grnet.endpoint.scanner.runtime.entities.pagination.Page;
import org.grnet.endpoint.scanner.runtime.entities.pagination.PageQueryImpl;
import org.grnet.endpoint.scanner.runtime.services.ResourceAuthorizationService;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service responsible for managing groups and their members through the external authorization provider.
 */
@ApplicationScoped
public class GroupManagementService {

    @Inject
    AuthGroupManagement groupManagement;

    @Inject
    Utility utility;

    @Inject
    ProviderRepository providerRepository;

    @Inject
    ResourceAuthorizationService resourceAuthorizationService;

    @ConfigProperty(name = "api.auth.entitlements.parent-group")
    String parentGroup;

    @ConfigProperty(name = "api.ui.url")
    String uiBaseUrl;

    @ConfigProperty(name = "api.auth.entitlements.parent-group")
    String namespace;

    @ConfigProperty(name = "api.members-group")
    String membersGroup;

    /**
     * Returns a paginated list of members for the given group.
     *
     * @param search optional search filter
     * @param page 0-based page index
     * @param size number of records per page
     * @param uriInfo request context for pagination links
     * @return paginated list of group members
     */
    public PageResource<GroupUserResponse> getAllMembers(String groupName, String search, int page, int size, UriInfo uriInfo) {

        var authPage = resourceAuthorizationService.getAllMembersByPageAndSize(page, size, search, null, uriInfo);

        var content = authPage.getContent()
                .stream()
                .map(this::resolveMemberResourceNames)
                .toList();

        var pageable = new PageQueryImpl<GroupUserResponse>();
        pageable.list = content;
        pageable.index = page;
        pageable.size = size;
        pageable.count = authPage.getTotalElements();
        pageable.page = Page.of(page, size);

        return new PageResource<>(pageable, uriInfo);
    }



    /**
     * Resolves a resource identifier to a display name when supported.
     *
     * @param resource resource type
     * @param resourceId resource identifier
     * @return resolved resource name or the original identifier
     */
    String resolveResourceName(String resource, String resourceId) {

        switch (resource) {

            case "Provider":
                return providerRepository.findByIdOptional(Integer.valueOf(resourceId))
                        .map(Provider::getName)
                        .orElse(resourceId);

            // Project
            // Invitation
            // More resources to come

            default:
                return resourceId;
        }
    }

    /**
     * Fetches members of a group directly from the authorization provider.
     *
     * @param groupName the group identifier
     * @param first starting index (offset)
     * @param max maximum number of results
     * @param search optional search filter
     * @return provider response containing members and total count
     */
    public GroupMembersResponse getMembers(String groupName, int first, int max, String search) {

        var fullPath = normalizePath(parentGroup) + "/" + groupName;

        return groupManagement.fetchGroupMembers(fullPath, first, max, search);
    }

    /**
     * Returns all application members assigned to a provider with the specified role.
     *
     * @param providerId provider identifier
     * @param role provider role (e.g. provider_admin, provider_member)
     * @return list of users assigned to the provider role
     */
    public List<GroupUserResponse> getProviderMembersByRole(String providerId, String role) {

        return getAllApplicationMembersRaw("")
                .stream()
                .filter(user -> hasProviderRole(user, providerId, role))
                .map(this::resolveMemberResourceNames)
                .toList();
    }


    /**
     * Checks whether a user has the specified provider role
     * based on local entitlement assignments.
     *
     * @param user AGM group user
     * @param providerId provider identifier
     * @param role provider role
     * @return true if the user has the provider role
     */
    private boolean hasProviderRole(GroupUserResponse user, String providerId, String role) {

        if (user.memberships == null || user.memberships.isEmpty()) {
            return false;
        }

        return user.memberships
                .getOrDefault(ProviderResource.PROVIDER.resourceName(), List.of())
                .stream()
                .filter(group -> providerId.equals(group.name))
                .anyMatch(group -> role.equals(group.role));
    }


    /**
     * Adds a user to a group with the default role "member".
     *
     * @param groupName the group identifier
     * @param username user identifier recognized by the auth provider
     */
    public void addMember(String groupName, String username) {

        var fullPath = normalizePath(parentGroup) + "/" + groupName;

        groupManagement.addGroupMember(fullPath, username, "member");
    }


    /**
     * Normalizes a group path.
     *
     * @param p raw path
     * @return normalized path
     */
    private static String normalizePath(String p) {
        if (p == null || p.isBlank()) return "";
        p = p.trim();

        // ensure leading slash
        if (!p.startsWith("/")) p = "/" + p;

        // remove trailing slash
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);

        return p;
    }

    /**
     * Returns a paginated list of groups from the authorization provider.
     *
     * @param page 0-based page index
     * @param size page size
     * @param uriInfo request context for pagination links
     * @return paginated list of groups
     */
    public PageResource<PartialGroup> fetchGroups(int page, int size, UriInfo uriInfo){

        var groups = groupManagement.fetchGroups();

        var partition = utility.partition(new ArrayList<>(groups), size);

        var pageableMembers = partition.get(page) == null ? Collections.EMPTY_LIST : partition.get(page);

        var pageable = new PageQueryImpl<PartialGroup>();

        pageable.list = pageableMembers;
        pageable.index = page;
        pageable.size = size;
        pageable.count = groups.size();
        pageable.page = Page.of(page, size);

        return new PageResource<>(pageable, uriInfo);
    }


    public List<GroupUserResponse> getAllApplicationMembersRaw(String search) {

        return resourceAuthorizationService.getApplicationMembers(
                groupManagement.getMembersGroupPath(),
                search
        );
    }

    private GroupUserResponse resolveMemberResourceNames(GroupUserResponse user) {

        if (user.memberships == null || user.memberships.isEmpty()) {
            return user;
        }

        user.memberships.forEach((resource, memberships) -> {
            if (memberships == null) {
                return;
            }

            memberships.forEach(membership ->
                    membership.name = resolveResourceName(resource, membership.name)
            );
        });

        return user;
    }

}
