package gr.grnet.pccapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import gr.grnet.pccapi.dto.invitation.PrefixInvitationResponse;
import gr.grnet.pccapi.dto.prefix.PrefixRequestDto;
import gr.grnet.pccapi.endpoint.PrefixInvitationEndpoint;
import gr.grnet.pccapi.enums.InvitationStatus;
import gr.grnet.pccapi.service.MailerService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;
import java.util.UUID;

@QuarkusTest
@TestHTTPEndpoint(PrefixInvitationEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class PrefixInvitationEndpointTest extends KeycloakTest {

    @InjectMock
    MailerService mailerService;

    private Integer prefixId;

    @BeforeEach
    void setupAuthorization() {
        mockSuperAdmin();
    }

    @BeforeEach
    void createPrefix() {

        prefixInvitationService.deleteAll();

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

    @Test
    public void createInvitation() {

        var response = createInvitation(
                "invited@test.com",
                "prefix_member");

        assertNotNull(response.id);
        assertEquals("invited@test.com", response.email);
        assertEquals("prefix_member", response.role);
        assertEquals(InvitationStatus.PENDING, response.status);
    }

    @Test
    public void createExistingPendingInvitationReturnsSameInvitation() {

        var first = createInvitation(
                "duplicate@test.com",
                "prefix_member");

        var second = createInvitation(
                "duplicate@test.com",
                "prefix_member");

        assertEquals(first.id, second.id);
        assertEquals(first.email, second.email);
        assertEquals(InvitationStatus.PENDING, second.status);
    }

    @Test
    public void getInvitations() {

        createInvitation(
                "first@test.com",
                "prefix_member");

        createInvitation(
                "second@test.com",
                "prefix_member");

        var response = authenticatedRequest()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertEquals(2, response.getInt("total_elements"));
        assertEquals(2, response.getList("content").size());
    }

    @Test
    public void searchInvitationsByEmail() {

        createInvitation(
                "search-me@test.com",
                "prefix_member");

        createInvitation(
                "other@test.com",
                "prefix_member");

        var response = authenticatedRequest()
                .queryParam("search", "search-me")
                .get()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertEquals(1, response.getInt("total_elements"));
        assertEquals(
                "search-me@test.com",
                response.getString("content[0].email"));
    }

    @Test
    public void getInvitationById() {

        var created = createInvitation(
                "get@test.com",
                "prefix_member");

        var response = authenticatedRequest()
                .get("/{invitation_id}", created.id)
                .then()
                .statusCode(200)
                .extract()
                .as(PrefixInvitationResponse.class);

        assertEquals(created.id, response.id);
        assertEquals("get@test.com", response.email);
        assertEquals("prefix_member", response.role);
        assertEquals(InvitationStatus.PENDING, response.status);
    }

    @Test
    public void revokeInvitation() {

        var created = createInvitation(
                "revoke@test.com",
                "prefix_member");

        var response = authenticatedRequest()
                .patch("/{invitation_id}", created.id)
                .then()
                .statusCode(200)
                .extract()
                .as(PrefixInvitationResponse.class);

        assertEquals(created.id, response.id);
        assertEquals("revoke@test.com", response.email);
        assertEquals(InvitationStatus.REVOKED, response.status);
    }

    @Test
    public void getInvitationNotFound() {

        authenticatedRequest()
                .get("/{invitation_id}",
                        "00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404);
    }

    @Test
    public void createInvitationInvalidPrefix() {

        var request = Map.of(
                "email", "invalid-prefix@test.com",
                "role", "prefix_member"
        );

        given()
                .header("Authorization", "Bearer " + adminToken)
                .pathParam("id", 1)
                .pathParam("prefix-id", 999999)
                .contentType(ContentType.JSON)
                .body(request)
                .post()
                .then()
                .statusCode(404);
    }

    private PrefixInvitationResponse createInvitation(String email, String role) {

        var request = Map.of(
                "email", email,
                "role", role
        );

        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .as(PrefixInvitationResponse.class);
    }

    private RequestSpecification authenticatedRequest() {

        return given()
                .header("Authorization", "Bearer " + adminToken)
                .pathParam("id", 1)
                .pathParam("prefix-id", prefixId);
    }
}