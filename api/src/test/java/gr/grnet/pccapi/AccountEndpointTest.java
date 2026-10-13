
package gr.grnet.pccapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import gr.grnet.pccapi.dto.prefix.PrefixRequestDto;
import gr.grnet.pccapi.endpoint.AccountEndpoint;
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
@TestHTTPEndpoint(AccountEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class AccountEndpointTest extends KeycloakTest {

    private Integer prefixId;

    @BeforeEach
    void setupAuthorization() {
        mockSuperAdmin();
    }

    @BeforeEach
    void createPrefix() {

        var request = new PrefixRequestDto()
                .setName("account-test-" + UUID.randomUUID())
                .setOwner("someone")
                .setStatus(2)
                .setUsedBy("someone else")
                .setLookUpServiceTypeId(2)
                .setContractTypeId(5)
                .setDomainId(1)
                .setServiceName("ACCOUNT-TEST-SERVICE")
                .setProviderId(1)
                .setResolvable(Boolean.TRUE)
                .setContactName("testname")
                .setContactEmail("test@test.com")
                .setContractEnd("2008-01-01");

        prefixId = prefixService.create(request).getId();
    }

    @Test
    public void createPrefixAccount() {

        var request = Map.of(
                "email", "account@test.com",
                "endpoint", "https://handle.example.org",
                "username", "handle_user");

        var response = authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .jsonPath();

        assertNotNull(response.getString("id"));
        assertEquals("account@test.com", response.getString("email"));
        assertEquals("https://handle.example.org", response.getString("endpoint"));
        assertEquals("handle_user", response.getString("username"));
        assertEquals(301, response.getInt("admin_index"));
    }

    @Test
    public void createAccountWithoutUsername() {

        var request = Map.of(
                "email", "account@test.com",
                "endpoint", "https://handle.example.org");

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    public void createAccountWithEmptyUsername() {

        var request = Map.of(
                "email", "account@test.com",
                "endpoint", "https://handle.example.org",
                "username", "");

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    public void createAccountWithBlankUsername() {

        var request = Map.of(
                "email", "account@test.com",
                "endpoint", "https://handle.example.org",
                "username", "   ");

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    public void createAccountWithNullUsername() {

        var request = new java.util.HashMap<String, Object>();
        request.put("email", "account@test.com");
        request.put("endpoint", "https://handle.example.org");
        request.put("username", null);

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    public void getAccounts() {

        createAccount();

        var response = authenticatedRequest()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertEquals(1, response.getInt("total_elements"));
        assertEquals(1, response.getList("content").size());
        assertEquals("handle_user", response.getString("content[0].username"));
    }

    @Test
    public void getAccountsWithSearch() {

        createAccount();

        var response = authenticatedRequest()
                .queryParam("search", "test")
                .get()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertEquals(1, response.getInt("total_elements"));
        assertEquals("example@test.com", response.getString("content[0].email"));
        assertEquals("handle_user", response.getString("content[0].username"));
    }

    @Test
    public void getAccountById() {

        var accountId = createAccount();

        var response = authenticatedRequest()
                .get("/{account-id}", accountId)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertEquals(accountId, response.getString("id"));
        assertEquals("example@test.com", response.getString("email"));
        assertEquals("https://example.org", response.getString("endpoint"));
        assertEquals("handle_user", response.getString("username"));
    }

    @Test
    public void updateAccount() {

        var accountId = createAccount();

        var request = Map.of(
                "email", "after@test.com",
                "endpoint", "https://after.example.org",
                "username", "updated_handle_user");

        var response = authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .put("/{account-id}", accountId)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertEquals(accountId, response.getString("id"));
        assertEquals("after@test.com", response.getString("email"));
        assertEquals("https://after.example.org", response.getString("endpoint"));
        assertEquals("updated_handle_user", response.getString("username"));
        assertEquals(301, response.getInt("admin_index"));

        var fetchedAccount = authenticatedRequest()
                .get("/{account-id}", accountId)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertEquals("updated_handle_user", fetchedAccount.getString("username"));
    }

    @Test
    public void updateAccountWithoutUsername() {

        var accountId = createAccount();

        var request = Map.of(
                "email", "after@test.com",
                "endpoint", "https://after.example.org");

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .put("/{account-id}", accountId)
                .then()
                .statusCode(400);
    }

    @Test
    public void updateAccountWithEmptyUsername() {

        var accountId = createAccount();

        var request = Map.of(
                "email", "after@test.com",
                "endpoint", "https://after.example.org",
                "username", "");

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .put("/{account-id}", accountId)
                .then()
                .statusCode(400);
    }

    @Test
    public void deleteAccount() {

        var accountId = createAccount();

        var response = authenticatedRequest()
                .delete("/{account-id}", accountId)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertEquals(
                "Account has been successfully deleted.",
                response.getString("message"));
    }

    @Test
    public void deletedAccountCannotBeFetched() {

        var accountId = createAccount();

        authenticatedRequest()
                .delete("/{account-id}", accountId)
                .then()
                .statusCode(200);

        authenticatedRequest()
                .get("/{account-id}", accountId)
                .then()
                .statusCode(404);
    }

    @Test
    public void getAccountsInvalidPrefix() {

        given()
                .header("Authorization", "Bearer " + adminToken)
                .pathParam("id", 1)
                .pathParam("prefix-id", 999999)
                .get()
                .then()
                .statusCode(404);
    }

    private String createAccount() {

        var request = Map.of(
                "email", "example@test.com",
                "endpoint", "https://example.org",
                "username", "handle_user");

        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getString("id");
    }

    private RequestSpecification authenticatedRequest() {

        return given()
                .header("Authorization", "Bearer " + adminToken)
                .pathParam("id", 1)
                .pathParam("prefix-id", prefixId);
    }
}
