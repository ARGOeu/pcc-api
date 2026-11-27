package gr.grnet.pccapi.dev;

import gr.grnet.pccapi.enums.resources.ProviderResource;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.grnet.endpoint.scanner.runtime.Scope;
import org.grnet.endpoint.scanner.runtime.dtos.AssignRoleRequest;
import org.grnet.endpoint.scanner.runtime.dtos.CreateRoleRequest;
import org.grnet.endpoint.scanner.runtime.dtos.SecuredEndpointAssignment;
import org.grnet.endpoint.scanner.runtime.dtos.SecuredEndpointPerRoleRequest;
import org.grnet.endpoint.scanner.runtime.services.ResourceAuthorizationService;
import org.grnet.endpoint.scanner.runtime.services.RoleEndpointService;

import java.util.List;
import java.util.Map;

@ApplicationScoped
@IfBuildProfile("dev")
public class PccAuthorizationInitializer {

    @Inject
    ResourceAuthorizationService resourceAuthorizationService;

    @Inject
    RoleEndpointService roleEndpointService;

    void onStart(@Observes StartupEvent event) {

        Log.info("Initializing PCC authorization data (dev mode)...");

        try {
            var providerAdminRole = getOrCreateRoleId(
                    "provider_admin", "Provider Admin",
                    "Role with administrative rights for a Provider"
            );

            var prefixAdminRole = getOrCreateRoleId(
                    "prefix_admin", "Prefix Admin",
                    "Role with administrative rights for a Prefix"
            );

            var prefixMemberRole = getOrCreateRoleId(
                    "prefix_member", "Prefix Member",
                    "Role with member rights for a Prefix"
            );

            var memberRole = getOrCreateRoleId(
                    "members", "members",
                    "Role with member rights in service"
            );

            // Assign user1 as provider_admin for Provider 1.
            var roleAssignment = new AssignRoleRequest();
            roleAssignment.apiResource = ProviderResource.PROVIDER.resourceName();
            roleAssignment.resourceId = "1";
            roleAssignment.role = "provider_admin";
            roleAssignment.username = "user1";

            resourceAuthorizationService.assignRoleToUser(roleAssignment);

            // members: exported endpoint assignments.
            assignEndpoints(memberRole, List.of(
                    // PATCH /invitations/{invitation_id}
                    endpoint("202bffaf046cd1793d527191c8271cc38328ef56a297f92df90b82dca4c33e99", Scope.ALL),
                    // GET /providers/{id}/prefixes/{prefix-id}
                    endpoint("1ee49aecd3805e9a274d65e00ff18876c8ae8cf77faf3ff0f20fd69f00ade76a", Scope.MINE),
                    endpoint("99a97c42b8a9b8368eb8e9f6bd5e4075ef1e03bec9cb482c5ea9898ffd11bdda", Scope.ALL),
                    endpoint("2a38b11caf34a869ffc0065efff6a5d383d8d3ed3d71f3dbae7424d3b8a486d2", Scope.ALL),
                    // GET /invitations
                    endpoint("c87a0a55b1681ed3dd006b5892cf0bc5b899bfb765b79abb31dd2bdda96ae825", Scope.MINE),
                    // GET /providers/{id}/prefixes/{prefix-id}/count
                    endpoint("ee2d1714ae914aed5067d3125213b5195caf6ae1b672ef1e52378d9fc1b8e79f", Scope.MINE),
                    endpoint("94c164ecfc385bf4bd23667a5dd5e6ee898664dbf71ab835ae5e46ddf5ec6d17", Scope.ALL),
                    endpoint("c72b0721f88b10cb698c2b360e7f364d05286dde682f11fadce2ee98b4485b82", Scope.ALL),
                    endpoint("3ad662750513ed4bf566fe85f5d484e5caad864f37bcd70d46fbafeb05f33843", Scope.ALL),
                    endpoint("4d912f8d368c4bc47a831e87ea05384c9aa091d7dd8b0c2f19d059873be55c22", Scope.ALL),
                    endpoint("ad468635b957e2566ca189db34d2351c8a9d9dc710d2ebbadbdbb2af770bd079", Scope.ALL),
                    // GET /providers/{id}/prefixes/{prefix-id}/resolvable
                    endpoint("4729118f00572fde1170ef99c9354b0edd386425945fb7a155622bc78300114f", Scope.MINE),
                    endpoint("66d46e730739a77c7ca94ded188479764e5ce036e2722179971d981097d6c938", Scope.ALL),
                    endpoint("1256277e33d37a7c890919ac4ba94fc5c8a733f813a7665d70977afce71b44b3", Scope.ALL),
                    endpoint("377a72fd1dfdbcc631f1b6236fa9db23de15e43157554e86123bc83ea6bc812d", Scope.ALL),
                    // GET /providers/{id}/prefixes
                    endpoint("02d219d73ac738e116f18c4ab31504f23203f5210746e8373e879cf14d0ed79e", Scope.MINE),
                    endpoint("358f995df48f12953b18ec31ed9e0c21f12d9fe046297c1f187365ab803ffa62", Scope.ALL),
                    endpoint("3f63f636cf0f3244b04bb1e6d93b5495fb52d552c7a2279166a8a4c4de237c07", Scope.ALL),
                    // GET /invitations/{invitation_id}
                    endpoint("8307041bdce77906c528e8c57780c5eb9e7e2bb7d15d4509538a770ecb544452", Scope.ALL)
            ));

            // provider_admin: exported endpoint assignments.
            assignEndpoints(providerAdminRole, List.of(
                    // GET /providers
                    endpoint("8852f9249d5b9bfeffc3e7bc4ea7b4605c7b5d47348a967a8c193c8296943573", Scope.MINE),
                    // GET /providers/{id}/prefixes/{prefix-id}
                    endpoint("1ee49aecd3805e9a274d65e00ff18876c8ae8cf77faf3ff0f20fd69f00ade76a", Scope.ALL),
                    endpoint("a341e6edfead3bd03fb1ec6941f5ef64bf5c86633e616b8a13990fa593f97b01", Scope.ALL),
                    // PUT /providers/{id}/prefixes/{prefix-id}
                    endpoint("e7ab0606fb11bce06132138841c35c43b0675145498aeea6aad7b2fde50f4ee5", Scope.ALL),
                    // POST /invitations
                    endpoint("05b8a79abdf0fbd6e3c69101bddb017a0702289f3ccde431d4f26b43a7c99b4d", Scope.ALL),
                    // GET /prefix-requests/by-provider
                    endpoint("97c7f2fd8c1f4e8d92c6f6a506a0b8b106b3da2e59025fc409c645cca3b20cdb", Scope.ALL),
                    // GET /invitations
                    endpoint("2fc22883fa79feb02c715633b466e2660ebe108080595970b70bb47b8d043df0", Scope.ALL),
                    // GET /providers/{id}/prefixes/{prefix-id}/accounts
                    endpoint("8fec8fa6ef0f2d85d29a74131139251fab2f463fb034e7e33f3a44d1596e2704", Scope.ALL),
                    // GET /providers/{id}/prefixes/{prefix-id}/count
                    endpoint("ee2d1714ae914aed5067d3125213b5195caf6ae1b672ef1e52378d9fc1b8e79f", Scope.ALL),
                    // GET /providers/{id}/prefixes/{prefix-id}/statistic
                    endpoint("540c95cbe820456477bc6505812d6659f4a5bdc488e5648eb3bb009c157d4ecc", Scope.ALL),
                    endpoint("05a4bcecb27b5ca38e11684510f0be705229668f6e3ee7aa2cb2fe0de2279158", Scope.ALL),
                    endpoint("3ad662750513ed4bf566fe85f5d484e5caad864f37bcd70d46fbafeb05f33843", Scope.ALL),
                    endpoint("4deb81f43287385fb3517b31741706a555e28146adbac11bb84b9f265155ba93", Scope.ALL),
                    endpoint("377a72fd1dfdbcc631f1b6236fa9db23de15e43157554e86123bc83ea6bc812d", Scope.ALL),
                    endpoint("94467f5fba1fb2253b382795e4f5f9210c521bdea2e1cb1b47c3bb53fa923f69", Scope.ALL),
                    endpoint("cdced8c947de16f93e46ede8cb645b4f705d46a25e36a43e9ba7ef3b44b8804c", Scope.ALL),
                    // PATCH /providers/{id}/prefixes/{prefix-id}
                    endpoint("373c11085f0c756d591b09ebdbd058c61fa9a05353b162e5bf8ceb1d5bd50533", Scope.ALL),
                    // GET /providers/{id}/prefixes/{prefix-id}/accounts/{account-id}
                    endpoint("94746d045476b1abd5c5303291b79747871725eb5cde7894e31b0440c4a74bbe", Scope.ALL),
                    endpoint("6ebf9dd7b6f0a2636dcba0e22361805a0ccbb60faae2d481af839da89ce9b315", Scope.ALL),
                    endpoint("d6ec54b20191b08c933df788b8f77d57f46ddefc8145bdf5a887cc91b7f3d10f", Scope.ALL),
                    // GET /prefix-requests/{id}/by-provider
                    endpoint("f6a65fd4e8633331041c8eb2454fc9c32f638cfda10ef0f2211f23eea5a77430", Scope.ALL),
                    // PUT /providers/{id}/prefixes/{prefix-id}/accounts/{account-id}
                    endpoint("5cfb74b8a54767f13a891e7f68b377d4454293a1c9b6c6883c8c8b939239fbb3", Scope.ALL),
                    // POST /providers/{id}/prefixes/{prefix-id}/statistic
                    endpoint("580281fd294508ded56ab3e2fae42a872c0bcbf7abff45449d3a94a9910e689f", Scope.ALL),
                    // PATCH /invitations/{invitation_id}
                    endpoint("5a311ee9b514d8f4f0fbdf78dae2718f1f26ff8798a85c79dfc19814871da5bd", Scope.ALL),
                    endpoint("c72b0721f88b10cb698c2b360e7f364d05286dde682f11fadce2ee98b4485b82", Scope.ALL),
                    endpoint("88850c8bbee196ec1515e865cf1c66d9c0055dc66b56e15ca1550692c7533402", Scope.ALL),
                    // GET /providers/{id}
                    endpoint("0cbe4fe6cdc1a333cbf9c6c04c8978abb6b7432aed2f9c8f9e157b3c3ce986c3", Scope.ALL),
                    // GET /invitations/{invitation_id}
                    endpoint("d0d357931c720f012a65f3cd3fb5e8db0fa57e2db1ffbc20587259c728064104", Scope.ALL),
                    endpoint("4d912f8d368c4bc47a831e87ea05384c9aa091d7dd8b0c2f19d059873be55c22", Scope.ALL),
                    // GET /providers/{id}/prefixes/{prefix-id}/resolvable
                    endpoint("4729118f00572fde1170ef99c9354b0edd386425945fb7a155622bc78300114f", Scope.ALL),
                    endpoint("1256277e33d37a7c890919ac4ba94fc5c8a733f813a7665d70977afce71b44b3", Scope.ALL),
                    // GET /providers/{id}/prefixes
                    endpoint("02d219d73ac738e116f18c4ab31504f23203f5210746e8373e879cf14d0ed79e", Scope.ALL),
                    endpoint("66d46e730739a77c7ca94ded188479764e5ce036e2722179971d981097d6c938", Scope.ALL),
                    // POST /providers/{id}/prefixes/{prefix-id}/accounts
                    endpoint("a3239c039f8ebf7ece03c13b508b82cb3bcd953f172f893b3334b4b9deb912c2", Scope.ALL),
                    endpoint("ca16310a30afdab69635736b05feb77012f6f79a7cc1aa538730dfdae00945b9", Scope.ALL),
                    // DELETE /providers/{id}/prefixes/{prefix-id}
                    endpoint("ce4b4ae6d8670e9e78a1b2e0180ccb8db5636bf9f40fc668edb3c51f5c952a26", Scope.ALL),
                    endpoint("79d3d85890a4370a798885b9af38df9d6e4a4ddc2cfaf2c70990572308b19c31", Scope.ALL),
                    // POST /providers/{id}/prefixes
                    endpoint("66eb318e7f943a03aa92b68716c350459235b58562b5686aa81900fadcd36f9d", Scope.ALL),
                    endpoint("dce3919e39697469c66c8edff3a83594e6ef570d861a675d32ee33bd624dcd19", Scope.ALL)
            ));

            // prefix_admin: exported endpoint assignments.
            assignEndpoints(prefixAdminRole, List.of(
                    // PATCH /invitations/{invitation_id}
                    endpoint("202bffaf046cd1793d527191c8271cc38328ef56a297f92df90b82dca4c33e99", Scope.ALL),
                    // GET /providers/{id}/prefixes/{prefix-id}/accounts/{account-id}
                    endpoint("94746d045476b1abd5c5303291b79747871725eb5cde7894e31b0440c4a74bbe", Scope.MINE),
                    // GET /providers/{id}/prefixes/{prefix-id}
                    endpoint("1ee49aecd3805e9a274d65e00ff18876c8ae8cf77faf3ff0f20fd69f00ade76a", Scope.MINE),
                    endpoint("6ebf9dd7b6f0a2636dcba0e22361805a0ccbb60faae2d481af839da89ce9b315", Scope.ALL),
                    // GET /providers
                    endpoint("8852f9249d5b9bfeffc3e7bc4ea7b4605c7b5d47348a967a8c193c8296943573", Scope.MINE),
                    endpoint("a341e6edfead3bd03fb1ec6941f5ef64bf5c86633e616b8a13990fa593f97b01", Scope.ALL),
                    // GET /invitations
                    endpoint("c87a0a55b1681ed3dd006b5892cf0bc5b899bfb765b79abb31dd2bdda96ae825", Scope.MINE),
                    // GET /providers/{id}/prefixes/{prefix-id}/accounts
                    endpoint("8fec8fa6ef0f2d85d29a74131139251fab2f463fb034e7e33f3a44d1596e2704", Scope.MINE),
                    // GET /providers/{id}/prefixes/{prefix-id}/count
                    endpoint("ee2d1714ae914aed5067d3125213b5195caf6ae1b672ef1e52378d9fc1b8e79f", Scope.MINE),
                    endpoint("c72b0721f88b10cb698c2b360e7f364d05286dde682f11fadce2ee98b4485b82", Scope.ALL),
                    // GET /providers/{id}/prefixes/{prefix-id}/statistic
                    endpoint("540c95cbe820456477bc6505812d6659f4a5bdc488e5648eb3bb009c157d4ecc", Scope.MINE),
                    endpoint("88850c8bbee196ec1515e865cf1c66d9c0055dc66b56e15ca1550692c7533402", Scope.ALL),
                    endpoint("3ad662750513ed4bf566fe85f5d484e5caad864f37bcd70d46fbafeb05f33843", Scope.ALL),
                    endpoint("4d912f8d368c4bc47a831e87ea05384c9aa091d7dd8b0c2f19d059873be55c22", Scope.ALL),
                    endpoint("ad468635b957e2566ca189db34d2351c8a9d9dc710d2ebbadbdbb2af770bd079", Scope.ALL),
                    // GET /providers/{id}/prefixes/{prefix-id}/resolvable
                    endpoint("4729118f00572fde1170ef99c9354b0edd386425945fb7a155622bc78300114f", Scope.MINE),
                    endpoint("66d46e730739a77c7ca94ded188479764e5ce036e2722179971d981097d6c938", Scope.ALL),
                    endpoint("1256277e33d37a7c890919ac4ba94fc5c8a733f813a7665d70977afce71b44b3", Scope.ALL),
                    endpoint("377a72fd1dfdbcc631f1b6236fa9db23de15e43157554e86123bc83ea6bc812d", Scope.ALL),
                    // GET /providers/{id}/prefixes
                    endpoint("02d219d73ac738e116f18c4ab31504f23203f5210746e8373e879cf14d0ed79e", Scope.MINE),
                    // GET /invitations/{invitation_id}
                    endpoint("8307041bdce77906c528e8c57780c5eb9e7e2bb7d15d4509538a770ecb544452", Scope.ALL),
                    endpoint("cdced8c947de16f93e46ede8cb645b4f705d46a25e36a43e9ba7ef3b44b8804c", Scope.ALL)
            ));

            // No endpoint assignments for prefix_member were present in the export.
            Log.info("PCC authorization data initialized.");
        } catch (Exception e) {
            Log.error("Failed to initialize PCC authorization data.", e);
        }
    }

    private String getOrCreateRoleId(String name, String preferredName, String description) {

        var existing = resourceAuthorizationService.getAllRoles()
                .stream()
                .filter(role -> name.equals(role.name))
                .findFirst();

        if (existing.isPresent()) {
            return existing.get().id;
        }

        var role = new CreateRoleRequest();
        role.name = name;
        role.attributes = Map.of(
                "preferred_name", List.of(preferredName),
                "description", List.of(description)
        );

        resourceAuthorizationService.createNewRole(role);

        return resourceAuthorizationService.getAllRoles()
                .stream()
                .filter(createdRole -> name.equals(createdRole.name))
                .findFirst()
                .orElseThrow()
                .id;
    }

    private void assignEndpoints(String roleId, List<SecuredEndpointAssignment> assignments) {

        var request = new SecuredEndpointPerRoleRequest();
        request.setAssignments(assignments);

        roleEndpointService.assignRolesToEndpointsPerRole(roleId, request);
    }

    private SecuredEndpointAssignment endpoint(String id, Scope scope) {

        var assignment = new SecuredEndpointAssignment();
        assignment.setSecuredEndpointId(id);
        assignment.setScope(scope);

        return assignment;
    }
}
