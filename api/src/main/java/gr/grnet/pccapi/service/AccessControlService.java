package gr.grnet.pccapi.service;

import io.quarkus.oidc.TokenIntrospection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.endpoint.scanner.runtime.entitlements.EntitlementProvider;
import org.grnet.endpoint.scanner.runtime.entitlements.EntitlementUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AccessControlService {

    @Inject
    EntitlementProvider entitlementProvider;

    @Inject
    TokenIntrospection tokenIntrospection;

    @ConfigProperty(name = "api.auth.entitlements.namespace")
    String namespace;

    @ConfigProperty(name = "api.auth.entitlements.parent-group")
    String parentGroup;


    /**
     * Resolves all accessible subgroup identifiers for the specified group name.
     *
     * @return list of accessible subgroup identifiers
     */
    public List<String> resolveAccessibleGroupsByName(String role,String resource) {

        var entitlements = fetchEntitlementsBySubGroupId(role,resource);

        return entitlements
                .stream()
                .map(Entitlement::getHierarchy)
                .filter(h -> h != null && !h.isEmpty())
                .map(h -> h.get(h.size() - 1))
                .collect(Collectors.toList());
    }


    /**
     * Checks whether the authenticated user has the super_admin role.
     *
     * @return true if user is super administrator
     */
    public boolean isSuperAdmin() {
        return entitlementProvider.isSuperAdmin();
    }


    /**
     * Retrieves and parses entitlements from the authenticated OIDC token filtered by subgroup.
     *
     * @return list of parsed entitlements
     */
    public List<Entitlement> fetchEntitlementsBySubGroupId(String role, String resource) {

        var arr = tokenIntrospection.getJsonObject().getJsonArray("entitlements");

        if (arr == null) {
            return Collections.emptyList();
        }

        var raws = arr.stream()
                .map(v -> v.toString().replace("\"", ""))
                .filter(s -> s.startsWith(namespace))           // filter by namespace
                .map(s -> s.replace(namespace + ":", ""))
                .filter(s -> s.startsWith("group:" + parentGroup + ":" +role + ":" + resource))
                .collect(Collectors.toList());

        return EntitlementUtils.parseEntitlements(raws);
    }
}
