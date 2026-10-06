package gr.grnet.pccapi;

import gr.grnet.pccapi.service.PrefixInvitationService;
import gr.grnet.pccapi.service.PrefixService;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.inject.Inject;

import org.grnet.endpoint.scanner.runtime.entities.RoleEndpoint;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.endpoint.scanner.runtime.repositories.RoleEndpointRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KeycloakTest {

    @Inject
    PrefixService prefixService;

    @Inject
    PrefixInvitationService prefixInvitationService;

    @Inject
    TestEntitlementProvider entitlementProvider;

    @Inject
    RoleEndpointRepository roleEndpointRepository;

    @TestHTTPResource
    URI baseUri;
    protected String adminToken;
    protected String providerAdminToken;
    protected String prefixMemberToken;
    protected String invitedUserToken;


    @BeforeAll
    public void setup() {
        RestAssured.baseURI = baseUri.toString();

        adminToken = getAccessToken("admin");
        providerAdminToken = getAccessToken("user1");
        prefixMemberToken = getAccessToken("user2");
        invitedUserToken = getAccessToken("user3");

        prefixInvitationService.deleteAll();
        prefixService.deleteAll();
    }

    protected String getAccessToken(String username) {
        return KeycloakTestClient.getAccessToken(username);
    }




    @BeforeEach
    void setupAuthorizationMocks() {

        var testRepo = new TestRoleEndpointRepository();

        QuarkusMock.installMockForType(
                testRepo,
                RoleEndpointRepository.class
        );

        this.roleEndpointRepository = testRepo;

        entitlementProvider.reset();
        testRepo.reset();
    }

    protected void mockSuperAdmin() {

        entitlementProvider.setSuperAdmin(true);
        entitlementProvider.setEntitlements(List.of());
    }

    protected void mockProviderAdmin(String providerId) {

        entitlementProvider.setSuperAdmin(false);

        entitlementProvider.setEntitlements(
                List.of(
                        entitlement(
                                providerId,
                                "provider_admin"
                        )
                )
        );
    }

    protected void mockProviderMember(String providerId) {

        entitlementProvider.setSuperAdmin(false);
        entitlementProvider.setEntitlements(
                List.of(entitlement(providerId, "provider_member"))
        );
    }

    protected Entitlement entitlement(String providerId, String role) {

        var raw = "urn:mace:grnet.gr:einfra:login-devel:group:pcc-api:"
                + role
                + ":PROVIDER:"
                + providerId
                + ":role=member";

        return new Entitlement("pcc-api", List.of("pcc-api", role, "PROVIDER", providerId), role, raw);
    }

    protected void mockRoleEndpoints(String role, String... endpoints) {

        var roleEndpoints = new ArrayList<RoleEndpoint>();

        for (int i = 0; i < endpoints.length; i++) {

            roleEndpoints.add(new RoleEndpoint(
                            (long) i + 1,
                            role,
                            role,
                            endpoints[i],
                            LocalDateTime.now(),
                            null
                    )
            );
        }

        ((TestRoleEndpointRepository) roleEndpointRepository)
                .set(roleEndpoints);
    }
}

