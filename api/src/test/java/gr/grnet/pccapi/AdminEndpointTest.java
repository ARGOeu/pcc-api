package gr.grnet.pccapi;

import gr.grnet.pccapi.endpoint.AdminEndpoint;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestHTTPEndpoint(AdminEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class AdminEndpointTest extends KeycloakTest {

    @BeforeEach
    void setupAuthorization() {
        mockSuperAdmin();
    }

    @Test
    public void getRoleMetadata() {

        var response = authenticatedRequest()
                .get("/roles/metadata")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertNotNull(response);
    }

    @Test
    public void getRoleAssignmentMetadata() {

        var response = authenticatedRequest()
                .get("/roles/assignment/metadata")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertNotNull(response.get("resources.Provider"));
        assertNotNull(response.get("resources.Prefix"));
    }

    private RequestSpecification authenticatedRequest() {

        return given()
                .header("Authorization", "Bearer " + adminToken);
    }
}