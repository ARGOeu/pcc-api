
package gr.grnet.pccapi;

import gr.grnet.pccapi.dto.invitation.InvitationResponse;
import gr.grnet.pccapi.dto.prefix.PrefixRequestDto;
import gr.grnet.pccapi.endpoint.InvitationEndpoint;
import gr.grnet.pccapi.enums.InvitationStatus;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.grnet.endpoint.scanner.runtime.dtos.CreateRoleRequest;
import org.grnet.endpoint.scanner.runtime.services.ResourceAuthorizationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(InvitationEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class InvitationEndpointTest extends KeycloakTest {
    
    @Inject
    ResourceAuthorizationService resourceAuthorizationService;

    private Integer prefixId;

    protected void setupTestRoles() {

        createRoleIfMissing(
                "provider_admin",
                "Provider Admin",
                "Role with administrative rights for a Provider"
        );

        createRoleIfMissing(
                "prefix_admin",
                "Prefix Admin",
                "Role with administrative rights for a Prefix"
        );

        createRoleIfMissing(
                "member",
                "Member",
                "Role with member rights in service"
        );
    }

    @BeforeEach
    void setupAuthorization() {
        mockSuperAdmin();
    }

    @BeforeAll
    void setupInvitationRoles() {
        setupTestRoles();
    }

    @BeforeEach
    void createPrefix() {

        invitationService.deleteAll();

        var request = new PrefixRequestDto()
                .setName("invitation-test-" + UUID.randomUUID())
                .setOwner("someone")
                .setStatus(2)
                .setUsedBy("someone else")
                .setLookUpServiceTypeId(2)
                .setContractTypeId(5)
                .setDomainId(1)
                .setServiceName("INVITATION-TEST-SERVICE")
                .setProviderId(1)
                .setResolvable(Boolean.TRUE)
                .setContactName("testname")
                .setContactEmail("test@test.com")
                .setContractEnd("2008-01-01");

        prefixId = prefixService.create(request).getId();
    }

    // --------------------------------------------------
    // VALIDATION METADATA
    // --------------------------------------------------

    @Test
    public void invitationValidationMetadataIsAvailable() {

        var resources = resourceAuthorizationService
                .getApiResourcesByPage(0, 100, null)
                .getContent();

        var roles = resourceAuthorizationService.getAllRoles();

        System.out.println("Available API resources:");
        resources.forEach(resource ->
                System.out.println(resource.getResourceName())
        );

        System.out.println("Available roles:");
        roles.forEach(role ->
                System.out.println(role.name)
        );

        assertTrue(
                resources.stream()
                        .anyMatch(resource ->
                                "Prefix".equals(resource.getResourceName())),
                "API resource Prefix is missing."
        );

        assertTrue(
                resources.stream()
                        .anyMatch(resource ->
                                "Provider".equals(resource.getResourceName())),
                "API resource Provider is missing."
        );

        assertTrue(
                roles.stream()
                        .anyMatch(role ->
                                "prefix_admin".equals(role.name)),
                "Role prefix_admin is missing."
        );

        assertTrue(
                roles.stream()
                        .anyMatch(role ->
                                "provider_admin".equals(role.name)),
                "Role provider_admin is missing."
        );
    }

    // --------------------------------------------------
    // CREATE
    // --------------------------------------------------

    @Test
    public void createInvitation() {

        var response = createInvitation(
                "invited@test.com",
                "Prefix",
                String.valueOf(prefixId),
                "prefix_admin"
        );

        assertNotNull(response.id);
        assertEquals("invited@test.com", response.email);
        assertEquals("Prefix", response.apiResource);
        assertEquals(String.valueOf(prefixId), response.resourceId);
        assertEquals("prefix_admin", response.role);
        assertEquals(InvitationStatus.PENDING, response.status);
    }

    @Test
    public void createExistingPendingInvitationReturnsSameInvitation() {

        var first = createInvitation(
                "duplicate@test.com",
                "Prefix",
                String.valueOf(prefixId),
                "prefix_admin"
        );

        var second = createInvitation(
                "duplicate@test.com",
                "Prefix",
                String.valueOf(prefixId),
                "prefix_admin"
        );

        assertEquals(first.id, second.id);
        assertEquals(first.email, second.email);
        assertEquals(first.apiResource, second.apiResource);
        assertEquals(first.resourceId, second.resourceId);
        assertEquals(InvitationStatus.PENDING, second.status);
    }

    @Test
    public void createProviderInvitation() {

        var response = createInvitation(
                "provider-member@test.com",
                "Provider",
                "1",
                "provider_admin"
        );

        assertNotNull(response.id);
        assertEquals("provider-member@test.com", response.email);
        assertEquals("Provider", response.apiResource);
        assertEquals("1", response.resourceId);
        assertEquals("provider_admin", response.role);
        assertEquals(InvitationStatus.PENDING, response.status);
    }

    @Test
    public void createInvitationInvalidResource() {

        var request = Map.of(
                "email", "invalid-prefix@test.com",
                "api_resource", "Prefix",
                "resource_id", "999999",
                "role", "prefix_admin"
        );

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    // --------------------------------------------------
    // GET ALL
    // --------------------------------------------------

    @Test
    public void getInvitations() {

        createInvitation(
                "first@test.com",
                "Prefix",
                String.valueOf(prefixId),
                "prefix_admin"
        );

        createInvitation(
                "second@test.com",
                "Prefix",
                String.valueOf(prefixId),
                "prefix_admin"
        );

        var response = authenticatedRequest()
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertEquals(2, response.getInt("total_elements"));
        assertEquals(2, response.getList("content").size());
    }

    @Test
    public void getInvitationsByApiResourceAndResourceId() {

        createInvitation(
                "first@test.com",
                "Prefix",
                String.valueOf(prefixId),
                "prefix_admin"
        );

        createInvitation(
                "provider@test.com",
                "Provider",
                "1",
                "provider_admin"
        );

        var response = authenticatedRequest()
                .queryParam("api_resource", "Prefix")
                .queryParam("resource_id", String.valueOf(prefixId))
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertEquals(1, response.getInt("total_elements"));
        assertEquals("Prefix", response.getString("content[0].api_resource"));
        assertEquals(
                String.valueOf(prefixId),
                response.getString("content[0].resource_id")
        );
        assertEquals("first@test.com", response.getString("content[0].email"));
    }

    @Test
    public void searchInvitationsByEmail() {

        createInvitation(
                "search-me@test.com",
                "Prefix",
                String.valueOf(prefixId),
                "prefix_admin"
        );

        createInvitation(
                "other@test.com",
                "Prefix",
                String.valueOf(prefixId),
                "prefix_admin"
        );

        var response = authenticatedRequest()
                .queryParam("search", "search-me")
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertEquals(1, response.getInt("total_elements"));
        assertEquals(
                "search-me@test.com",
                response.getString("content[0].email")
        );
    }

    // --------------------------------------------------
    // GET BY ID
    // --------------------------------------------------

    @Test
    public void getInvitationById() {

        var created = createInvitation(
                "get@test.com",
                "Prefix",
                String.valueOf(prefixId),
                "prefix_admin"
        );

        var response = authenticatedRequest()
                .get("/{invitation_id}", created.id)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .as(InvitationResponse.class);

        assertEquals(created.id, response.id);
        assertEquals("get@test.com", response.email);
        assertEquals("Prefix", response.apiResource);
        assertEquals(String.valueOf(prefixId), response.resourceId);
        assertEquals("prefix_admin", response.role);
        assertEquals(InvitationStatus.PENDING, response.status);
    }

    @Test
    public void getInvitationNotFound() {

        authenticatedRequest()
                .get(
                        "/{invitation_id}",
                        "00000000-0000-0000-0000-000000000000"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    // --------------------------------------------------
    // REVOKE
    // --------------------------------------------------

    @Test
    public void revokeInvitation() {

        var created = createInvitation(
                "revoke@test.com",
                "Prefix",
                String.valueOf(prefixId),
                "prefix_admin"
        );

        var response = authenticatedRequest()
                .patch("/{invitation_id}", created.id)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .as(InvitationResponse.class);

        assertEquals(created.id, response.id);
        assertEquals("revoke@test.com", response.email);
        assertEquals("Prefix", response.apiResource);
        assertEquals(String.valueOf(prefixId), response.resourceId);
        assertEquals(InvitationStatus.REVOKED, response.status);
    }

    // --------------------------------------------------
    // HELPERS
    // --------------------------------------------------

    private InvitationResponse createInvitation(
            String email,
            String apiResource,
            String resourceId,
            String role) {

        var request = Map.of(
                "email", email,
                "api_resource", apiResource,
                "resource_id", resourceId,
                "role", role
        );

        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .extract()
                .as(InvitationResponse.class);
    }

    private RequestSpecification authenticatedRequest() {

        return given()
                .header("Authorization", "Bearer " + adminToken);
    }



    private void createRoleIfMissing(
            String name,
            String preferredName,
            String description) {

        var exists = resourceAuthorizationService.getAllRoles()
                .stream()
                .anyMatch(role -> name.equals(role.name));

        if (exists) {
            return;
        }

        var role = new CreateRoleRequest();

        role.name = name;
        role.attributes = Map.of(
                "preferred_name", List.of(preferredName),
                "description", List.of(description)
        );

        resourceAuthorizationService.createNewRole(role);
    }
}