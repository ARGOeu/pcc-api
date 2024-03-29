package gr.grnet.pccapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.ProviderResponseDTO;
import gr.grnet.pccapi.endpoint.ProviderEndpoint;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestHTTPEndpoint(ProviderEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProviderEndpointTest {

  /** Test the /providers/{id} endpoint */
  @Test
  public void getById() {
    var response =
        given()
            .contentType(ContentType.JSON)
            .get("1")
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(ProviderResponseDTO.class);

    assertEquals(1, response.id);
    assertEquals("GRNET", response.name);
  }

  /** Test the /providers endpoint */
  @Test
  public void getList() {
    var response =
        given()
            .contentType(ContentType.JSON)
            .get()
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(ProviderResponseDTO[].class);

    assertEquals(4, response.length);

    assertEquals(1, response[0].id);
    assertEquals("GRNET", response[0].name);

    assertEquals(2, response[1].id);
    assertEquals("DKRZ", response[1].name);

    assertEquals(3, response[2].id);
    assertEquals("SURF", response[2].name);

    assertEquals(4, response[3].id);
    assertEquals("GWDG", response[3].name);
  }

  @Test
  public void getByIdNotfound() {
    var response =
        given()
            .contentType(ContentType.JSON)
            .get("/{id}", 999)
            .then()
            .assertThat()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Provider not found", response.getMessage());
  }
}
