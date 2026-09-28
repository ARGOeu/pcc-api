package gr.grnet.pccapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.provider.ProviderResponseDTO;
import gr.grnet.pccapi.endpoint.ProviderEndpoint;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestHTTPEndpoint(ProviderEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class ProviderEndpointTest {

  @KeycloakToken(username = "admin", password = "admin")
  String adminToken;

  @Test
  public void getById() {

    var response = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .get("/{id}", 1)
            .then()
            .statusCode(200)
            .extract()
            .as(ProviderResponseDTO.class);

    assertEquals(1, response.id);
    assertEquals("GRNET", response.name);
  }

  @Test
  public void getList() {

    var response = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .get()
            .then()
            .statusCode(200)
            .extract()
            .as(ProviderResponseDTO[].class);

    assertEquals(3, response.length);

    assertTrue(Arrays.stream(response)
            .anyMatch(provider -> provider.id == 1 && "GRNET".equals(provider.name)));

    assertTrue(Arrays.stream(response)
            .anyMatch(provider -> provider.id == 3 && "Surf".equals(provider.name)));

    assertTrue(Arrays.stream(response)
            .anyMatch(provider -> provider.id == 4 && "GWDG".equals(provider.name)));
  }

  @Test
  public void getByIdNotfound() {

    var response = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .get("/{id}", 999)
            .then()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Provider not found", response.getMessage());
  }
}