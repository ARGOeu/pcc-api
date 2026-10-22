package gr.grnet.pccapi.service;

import gr.grnet.pccapi.entity.Provider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.response.UserGroupInfoDto;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.endpoint.scanner.runtime.entitlements.EntitlementProvider;
import org.grnet.endpoint.scanner.runtime.entitlements.EntitlementUtils;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class UserEntitlementsService {

    @Inject
    EntitlementProvider entitlementProvider;

    @Inject
    AccessControlService accessControlService;

    @Inject
    GroupManagementService groupManagementService;
    /**
     * Retrieves the authenticated user's entitlements and converts them to group information.
     *
     * @return list of user group information
     */
    public List<UserGroupInfoDto> getUserEntitlements() {

        var entitlements = entitlementProvider.fetchEntitlements();

        var groups = new ArrayList<UserGroupInfoDto>();

        groups.addAll(entitlements.stream()
                .filter(entitlement -> entitlement.getHierarchy().size() <= 2)
                .map(this::toInfo)
                .toList());

        groups.addAll(EntitlementUtils.extractResourceRoles(entitlements)
                .stream()
                .map(entitlement -> {
                    var dto = new UserGroupInfoDto();
                    dto.name = groupManagementService.resolveResourceName(entitlement.resource(), entitlement.resourceId());
                    dto.role = entitlement.role();
                    return dto;
                })
                .toList());

        return groups;
    }

    /**
     * Converts an entitlement into a user group information DTO.
     *
     * @param e entitlement
     * @return user group information
     */
    private UserGroupInfoDto toInfo(Entitlement e) {

        var info = new UserGroupInfoDto();
        info.role = e.getRole();

        var hierarchy = e.getHierarchy();

        if (hierarchy != null && !hierarchy.isEmpty()) {
            // subgroup = last element before :role
            info.name = hierarchy.get(hierarchy.size() - 1);
        }

        return info;
    }

    public List<String> getAdministeredProviderNames() {
        var entitlements = entitlementProvider.fetchEntitlements();

        return EntitlementUtils.extractResourceRoles(entitlements).stream()
                .filter(e -> Provider.class.getSimpleName().equals(e.resource()))
                .filter(e -> "provider_admin".equalsIgnoreCase(e.role()))
                .map(e -> groupManagementService.resolveResourceName(
                        e.resource(), e.resourceId()))
                .distinct()
                .toList();
    }
}
