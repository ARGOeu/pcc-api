package gr.grnet.pccapi;


import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import lombok.Setter;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.endpoint.scanner.runtime.entitlements.EntitlementProvider;

import java.util.List;
@Setter
@Alternative
@Priority(1)
@ApplicationScoped
public class TestEntitlementProvider implements EntitlementProvider {

    private List<Entitlement> entitlements = List.of();
    // ✅ TEST CONTROL API (important)
    private boolean superAdmin = false;

    @Override
    public List<Entitlement> fetchEntitlements() {
        return entitlements;
    }

    @Override
    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public void reset() {
        this.superAdmin = false;
        this.entitlements = List.of();
    }
}