package gr.grnet.pccapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

@QuarkusTest
@TestHTTPEndpoint(PrefixRequestEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class PrefixRequestEndpointTest extends KeycloakTest {


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

    private Response createPrefixRequest(String requestBody) {
        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .response();
    }

    private Response getPrefixRequest(Integer id) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get("/{id}", id)
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    private Response updatePrefixRequest(
            Integer id,
            String requestBody) {

        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/{id}", id)
                .then()
                .statusCode(201)
                .extract()
                .response();
    }

    @Test
    void createPrefixRequest() {
        String email = "prefix-create-test@example.com";
        String projectName = "PREFIX-CREATE-TEST";

        Response response = createPrefixRequest(
                prefixRequestJson(
                        email,
                        projectName,
                        "https://prefix-create-test.example.com"));

        assertNotNull(response.jsonPath().get("id"));
        assertEquals(
                email,
                response.jsonPath().getString("contactPersonEmail"));
        assertEquals(
                projectName,
                response.jsonPath().getString("projectName"));
        assertEquals(
                "UP_TO_1000",
                response.jsonPath().getString("expectedHandles"));
    }

    @Test
    void getPrefixRequestById() {
        Response created = createPrefixRequest(
                prefixRequestJson(
                        "prefix-get-test@example.com",
                        "PREFIX-GET-TEST",
                        "https://prefix-get-test.example.com"));

        Integer id = created.jsonPath().getInt("id");

        Response response = getPrefixRequest(id);

        assertEquals(id, response.jsonPath().getInt("id"));
        assertEquals(
                "prefix-get-test@example.com",
                response.jsonPath().getString("contactPersonEmail"));
        assertEquals(
                "PREFIX-GET-TEST",
                response.jsonPath().getString("projectName"));
    }

    @Test
    void getPrefixRequestByIdNotFound() {
        given()
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

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Assumes the list endpoint returns a JSON array.
        String responseBody = response.asString();

        assertNotNull(responseBody);
        assertTrue(responseBody.contains("PREFIX-LIST-TEST"));
    }

    @Test
    void updatePrefixRequest() {
        Response created = createPrefixRequest(
                prefixRequestJson(
                        "prefix-update-test@example.com",
                        "PREFIX-UPDATE-TEST",
                        "https://prefix-update-test.example.com"));

        Integer id = created.jsonPath().getInt("id");

        String updatedEmail = "prefix-updated-test@example.com";
        String updatedProjectName = "PREFIX-UPDATED-TEST";

        Response response = updatePrefixRequest(
                id,
                prefixRequestJson(
                        updatedEmail,
                        updatedProjectName,
                        "https://prefix-updated-test.example.com"));

        assertEquals(id, response.jsonPath().getInt("id"));
        assertEquals(
                updatedEmail,
                response.jsonPath().getString("contactPersonEmail"));
        assertEquals(
                updatedProjectName,
                response.jsonPath().getString("projectName"));

        // Verify that updatedAt is populated after the update.
        assertNotNull(response.jsonPath().get("updatedAt"));
    }

    @Test
    void updatePrefixRequestNotFound() {
        given()
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
        Response created = createPrefixRequest(
                prefixRequestJson(
                        "prefix-delete-test@example.com",
                        "PREFIX-DELETE-TEST",
                        "https://prefix-delete-test.example.com"));

        Integer id = created.jsonPath().getInt("id");

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/{id}", id)
                .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/{id}", id)
                .then()
                .statusCode(404);
    }

    @Test
    void deletePrefixRequestNotFound() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/{id}", 999999)
                .then()
                .statusCode(404);
    }
}
