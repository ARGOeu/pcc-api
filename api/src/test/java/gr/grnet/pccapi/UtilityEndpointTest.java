package gr.grnet.pccapi;

import gr.grnet.pccapi.endpoint.UtilityEndpoint;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestHTTPEndpoint(UtilityEndpoint.class)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class UtilityEndpointTest extends KeycloakTest {

    @BeforeEach
    void setupAuthorization() {
        mockMember();
        mockRoleEndpoints("member", "POST_/api/v1/validate-url");

    }

    private RequestSpecification request() {
        return given()
                .auth()
                .oauth2(member)
                .contentType(ContentType.JSON);
    }

    @Test
    public void invalidUrl() {
        request().body(Map.of("url", "ht!tp://invalid-url"))
                .when().post("/validate-url")
                .then().statusCode(200)
                .body("valid", equalTo(false))
                .body("reachable", equalTo(false))
                .body("message", equalTo("Invalid URL format"));
    }

    @Test
    public void missingScheme() {
        request().body(Map.of("url", "example.com"))
                .when().post("/validate-url")
                .then().statusCode(200)
                .body("valid", equalTo(false))
                .body("reachable", equalTo(false));
    }

    @Test
    public void unsupportedScheme() {
        request().body(Map.of("url", "ftp://example.com"))
                .when().post("/validate-url")
                .then().statusCode(200)
                .body("valid", equalTo(false))
                .body("reachable", equalTo(false));
    }

    @Test
    public void malformedUrl() {
        request().body(Map.of("url", "https://"))
                .when().post("/validate-url")
                .then().statusCode(200)
                .body("valid", equalTo(false))
                .body("reachable", equalTo(false));
    }

    @Test
    public void missingUrl() {
        request().body(Map.of())
                .when().post("/validate-url")
                .then().statusCode(400);
    }

    @Test
    public void blankUrl() {
        request().body(Map.of("url", ""))
                .when().post("/validate-url")
                .then().statusCode(400);
    }

    @Test
    public void unauthenticated() {
        given().contentType(ContentType.JSON)
                .body(Map.of("url", "https://example.com"))
                .when().post("/validate-url")
                .then().statusCode(401);
    }
}