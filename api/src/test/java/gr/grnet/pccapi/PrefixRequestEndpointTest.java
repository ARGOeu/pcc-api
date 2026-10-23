package gr.grnet.pccapi;

import gr.grnet.pccapi.dto.PrefixRequestResponse;
import gr.grnet.pccapi.endpoint.PrefixRequestEndpoint;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(PrefixRequestEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class PrefixRequestEndpointTest extends KeycloakTest {

    @BeforeEach
    void setupAuthorization() {
        mockSuperAdmin();
        prefixRequestService.deleteAll();
    }

    private String prefixRequestJson(
            String email,
            String projectName,
            String projectUrl) {

        return """
                {
                  "contactPersonName": "Test",
                  "contactPersonSurname": "User",
                  "contactPersonEmail": "%s",
                  "contactPersonPhone": "2103911103",
                  "country": "Greece",
                  "organizationName": "GRNET",
                  "projectName": "%s",
                  "projectUrl": "%s",
                  "projectDescription": "Test project description",
                  "expected_handles": "UP_TO_1000"
                }
                """.formatted(email, projectName, projectUrl);
    }

    private PrefixRequestResponse createPrefixRequest(String requestBody) {
        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .as(PrefixRequestResponse.class);
    }

    private PrefixRequestResponse getPrefixRequest(Integer id) {
        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .when()
                .get("/{id}", id)
                .then()
                .statusCode(200)
                .extract()
                .as(PrefixRequestResponse.class);
    }

    private PrefixRequestResponse updatePrefixRequest(
            Integer id,
            String requestBody) {

        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/{id}", id)
                .then()
                .statusCode(201)
                .extract()
                .as(PrefixRequestResponse.class);
    }

    @Test
    void createPrefixRequest() {
        String email = "prefix-create-test@example.com";
        String projectName = "PREFIX-CREATE-TEST";

        PrefixRequestResponse response = createPrefixRequest(
                prefixRequestJson(
                        email,
                        projectName,
                        "https://prefix-create-test.example.com"));

        assertNotNull(response.id);
        assertEquals(email,response.contactPersonEmail);
        assertEquals(projectName, response.projectName);
        assertEquals("UP_TO_1000", response.expectedHandles);
    }

    @Test
    void getPrefixRequestById() {
        PrefixRequestResponse created = createPrefixRequest(
                prefixRequestJson(
                        "prefix-get-test@example.com",
                        "PREFIX-GET-TEST",
                        "https://prefix-get-test.example.com"));

        Integer id = created.id;

        PrefixRequestResponse response = getPrefixRequest(id);

        assertNotNull(response.id);
        assertEquals("prefix-get-test@example.com", response.contactPersonEmail);
        assertEquals("PREFIX-GET-TEST", response.projectName);

    }

    @Test
    void getPrefixRequestByIdNotFound() {
        authenticatedRequest()
                .contentType(ContentType.JSON)
                .when()
                .get("/{id}", 999999)
                .then()
                .statusCode(404);
    }

    @Test
    void getPrefixRequestList() {
        createPrefixRequest(
                prefixRequestJson(
                        "prefix-list-test@example.com",
                        "PREFIX-LIST-TEST",
                        "https://prefix-list-test.example.com"));

        List response = authenticatedRequest()
                .contentType(ContentType.JSON)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .as(List.class);

        // Assumes the list endpoint returns a JSON array.
       // String responseBody = response.asString();


        assertEquals(1,response.size());
    }

    @Test
    void updatePrefixRequest() {
        PrefixRequestResponse created = createPrefixRequest(
                prefixRequestJson(
                        "prefix-update-test@example.com",
                        "PREFIX-UPDATE-TEST",
                        "https://prefix-update-test.example.com"));

        Integer id = created.id;


        String updatedEmail = "prefix-updated-test@example.com";
        String updatedProjectName = "PREFIX-UPDATED-TEST";

        PrefixRequestResponse response = updatePrefixRequest(
                id,
                prefixRequestJson(
                        updatedEmail,
                        updatedProjectName,
                        "https://prefix-updated-test.example.com"));

        assertEquals(id, response.id);
        assertEquals(
                updatedEmail,
                response.contactPersonEmail);
        assertEquals(
                updatedProjectName,
                response.projectName);

    }

    @Test
    void updatePrefixRequestNotFound() {
        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(prefixRequestJson(
                        "prefix-missing-test@example.com",
                        "PREFIX-MISSING-TEST",
                        "https://prefix-missing-test.example.com"))
                .when()
                .patch("/{id}", 999999)
                .then()
                .statusCode(404);
    }

    @Test
    void deletePrefixRequest() {
        PrefixRequestResponse created = createPrefixRequest(
                prefixRequestJson(
                        "prefix-delete-test@example.com",
                        "PREFIX-DELETE-TEST",
                        "https://prefix-delete-test.example.com"));

        Integer id = created.id;

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .when()
                .delete("/{id}", id)
                .then()
                .statusCode(200);

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .when()
                .get("/{id}", id)
                .then()
                .statusCode(404);
    }

    @Test
    void deletePrefixRequestNotFound() {
        authenticatedRequest()
                .contentType(ContentType.JSON)
                .when()
                .delete("/{id}", 999999)
                .then()
                .statusCode(404);
    }

    private RequestSpecification authenticatedRequest() {


        return given()
                .header("Authorization", "Bearer " + adminToken);
    }
}
