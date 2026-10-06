package gr.grnet.pccapi.dev;

import gr.grnet.pccapi.resources.ProviderResource;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.grnet.endpoint.scanner.runtime.Scope;
import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.AuthGroupManagement;
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

            // Create provider_admin role if it does not already exist
            var providerAdminRole = resourceAuthorizationService.getAllRoles()
                    .stream()
                    .filter(role -> "provider_admin".equals(role.name))
                    .findFirst()
                    .orElseGet(() -> {

                        var role = new CreateRoleRequest();
                        role.name = "provider_admin";
                        role.attributes = Map.of(
                                "preferred_name", List.of("Provider Admin"),
                                "description", List.of(
                                        "Role with administrative rights for a Provider"
                                )
                        );

                        resourceAuthorizationService.createNewRole(role);

                        return resourceAuthorizationService.getAllRoles()
                                .stream()
                                .filter(createdRole ->
                                        "provider_admin".equals(createdRole.name))
                                .findFirst()
                                .orElseThrow();
                    });


// Create prefix_member role if it does not already exist
            var providerMemberRole = resourceAuthorizationService.getAllRoles()
                    .stream()
                    .filter(role -> "prefix_member".equals(role.name))
                    .findFirst()
                    .orElseGet(() -> {

                        var role = new CreateRoleRequest();
                        role.name = "prefix_member";
                        role.attributes = Map.of(
                                "preferred_name", List.of("Prefix Member"),
                                "description", List.of(
                                        "Role with member rights for a Prefix"
                                )
                        );

                        resourceAuthorizationService.createNewRole(role);

                        return resourceAuthorizationService.getAllRoles()
                                .stream()
                                .filter(createdRole ->
                                        "prefix_member".equals(createdRole.name))
                                .findFirst()
                                .orElseThrow();
                    });

            // Assign user1 as provider_admin for Provider 1
            var roleAssignment = new AssignRoleRequest();
            roleAssignment.apiResource = ProviderResource.PROVIDER.resourceName();
            roleAssignment.resourceId = "1";
            roleAssignment.role = "provider_admin";
            roleAssignment.username = "user1";

            resourceAuthorizationService.assignRoleToUser(roleAssignment);

            // Assign Provider and Prefix endpoints to provider_admin
            var endpointRequest = new SecuredEndpointPerRoleRequest();

            endpointRequest.setAssignments(List.of(

                    // ---------------------------------------------------------------------
                    // PROVIDER
                    // ---------------------------------------------------------------------

                    // GET /providers
                    endpoint(
                            "8852f9249d5b9bfeffc3e7bc4ea7b4605c7b5d47348a967a8c193c8296943573",
                            Scope.MINE
                    ),

                    // GET /providers/{id}
                    endpoint(
                            "0cbe4fe6cdc1a333cbf9c6c04c8978abb6b7432aed2f9c8f9e157b3c3ce986c3",
                            null
                    ),

                    // ---------------------------------------------------------------------
                    // PREFIX
                    // ---------------------------------------------------------------------

                    // POST /providers/{id}/prefixes
                    endpoint(
                            "66eb318e7f943a03aa92b68716c350459235b58562b5686aa81900fadcd36f9d",
                            null
                    ),

                    // GET /providers/{id}/prefixes
                    endpoint(
                            "02d219d73ac738e116f18c4ab31504f23203f5210746e8373e879cf14d0ed79e",
                            Scope.MINE
                    ),

                    // GET /providers/{id}/prefixes/{prefix-id}
                    endpoint(
                            "1ee49aecd3805e9a274d65e00ff18876c8ae8cf77faf3ff0f20fd69f00ade76a",
                            Scope.MINE
                    ),

                    // PUT /providers/{id}/prefixes/{prefix-id}
                    endpoint(
                            "e7ab0606fb11bce06132138841c35c43b0675145498aeea6aad7b2fde50f4ee5",
                            null
                    ),

                    // PATCH /providers/{id}/prefixes/{prefix-id}
                    endpoint(
                            "373c11085f0c756d591b09ebdbd058c61fa9a05353b162e5bf8ceb1d5bd50533",
                            null
                    ),

                    // DELETE /providers/{id}/prefixes/{prefix-id}
                    endpoint(
                            "ce4b4ae6d8670e9e78a1b2e0180ccb8db5636bf9f40fc668edb3c51f5c952a26",
                            null
                    ),

                    // ---------------------------------------------------------------------
                    // PREFIX STATISTICS
                    // ---------------------------------------------------------------------

                    // GET /providers/{id}/prefixes/{prefix-id}/count
                    endpoint(
                            "ee2d1714ae914aed5067d3125213b5195caf6ae1b672ef1e52378d9fc1b8e79f",
                            Scope.MINE
                    ),

                    // GET /providers/{id}/prefixes/{prefix-id}/resolvable
                    endpoint(
                            "4729118f00572fde1170ef99c9354b0edd386425945fb7a155622bc78300114f",
                            Scope.MINE
                    ),

                    // GET /providers/{id}/prefixes/{prefix-id}/statistic
                    endpoint(
                            "540c95cbe820456477bc6505812d6659f4a5bdc488e5648eb3bb009c157d4ecc",
                            Scope.MINE
                    ),

                    // POST /providers/{id}/prefixes/{prefix-id}/statistic
                    endpoint(
                            "580281fd294508ded56ab3e2fae42a872c0bcbf7abff45449d3a94a9910e689f",
                            null
                    ),

                    // ---------------------------------------------------------------------
                    // ACCOUNTS
                    // ---------------------------------------------------------------------

                    // POST /providers/{id}/prefixes/{prefix-id}/accounts
                    endpoint(
                            "a3239c039f8ebf7ece03c13b508b82cb3bcd953f172f893b3334b4b9deb912c2",
                            null
                    ),

                    // GET /providers/{id}/prefixes/{prefix-id}/accounts
                    endpoint(
                            "8fec8fa6ef0f2d85d29a74131139251fab2f463fb034e7e33f3a44d1596e2704",
                            Scope.MINE
                    ),

                    // GET /providers/{id}/prefixes/{prefix-id}/accounts/{account-id}
                    endpoint(
                            "94746d045476b1abd5c5303291b79747871725eb5cde7894e31b0440c4a74bbe",
                            Scope.MINE
                    ),

                    // PUT /providers/{id}/prefixes/{prefix-id}/accounts/{account-id}
                    endpoint(
                            "5cfb74b8a54767f13a891e7f68b377d4454293a1c9b6c6883c8c8b939239fbb3",
                            null
                    ),

                    // DELETE /providers/{id}/prefixes/{prefix-id}/accounts/{account-id}
                    endpoint(
                            "947fc5d10a841271537fca92c6d2928cd1a91becb9e6acb2fb3d4c3d9cc2fe8d",
                            null
                    ),

                    // ---------------------------------------------------------------------
                    // INVITATIONS
                    // ---------------------------------------------------------------------

                    // POST /providers/{id}/prefixes/{prefix-id}/invitations
                    endpoint(
                            "94467f5fba1fb2253b382795e4f5f9210c521bdea2e1cb1b47c3bb53fa923f69",
                            null
                    ),

                    // GET /providers/{id}/prefixes/{prefix-id}/invitations
                    endpoint(
                            "d6ec54b20191b08c933df788b8f77d57f46ddefc8145bdf5a887cc91b7f3d10f",
                            Scope.MINE
                    ),

                    // GET /providers/{id}/prefixes/{prefix-id}/invitations/{invitation_id}
                    endpoint(
                            "dce3919e39697469c66c8edff3a83594e6ef570d861a675d32ee33bd624dcd19",
                            Scope.MINE
                    ),

                    // PATCH /providers/{id}/prefixes/{prefix-id}/invitations/{invitation_id}
                    endpoint(
                            "ca16310a30afdab69635736b05feb77012f6f79a7cc1aa538730dfdae00945b9",
                            null
                    ),

                    // ---------------------------------------------------------------------
                    // HANDLES
                    // ---------------------------------------------------------------------

                    // POST /providers/{id}/prefixes/{id}/handles
                    endpoint(
                            "afda0a2462772366b13a4e72014c4c9a3f462881cb7fcbd30ba12b3dd92fec0f",
                            null
                    ),

                    // GET /providers/{id}/prefixes/{id}/handles
                    endpoint(
                            "1d2624c16241e1586c2f64aed75fa342f00dbd4b96caa1ff9a770e40adb83841",
                            null
                    ),

                    // GET /providers/{id}/prefixes/{id}/handles/{suffix}
                    endpoint(
                            "3967934be59ea9a298a968ca2b3c8b18209cbb9193dbbbe7a95ebd27aa874850",
                            null
                    ),

                    // PUT /providers/{id}/prefixes/{id}/handles/{suffix}
                    endpoint(
                            "e0d5b2afe3a5c9941832836bba2d4c3c90979bf79524334873f5b2995cabbf4e",
                            null
                    ),

                    // DELETE /providers/{id}/prefixes/{id}/handles/{suffix}
                    endpoint(
                            "b46b32ab387010d90ef1aa4ab980be05ad4311d670bbbe33f1d692aec1c79834",
                            null
                    )
            ));

            roleEndpointService.assignRolesToEndpointsPerRole(
                    providerAdminRole.id,
                    endpointRequest
            );

            Log.info("PCC authorization data initialized.");

        } catch (Exception e) {
            Log.error("Failed to initialize PCC authorization data.", e);
        }
    }

    private SecuredEndpointAssignment endpoint(String id, Scope scope) {

        var assignment = new SecuredEndpointAssignment();
        assignment.setSecuredEndpointId(id);
        assignment.setScope(scope);

        return assignment;
    }
}