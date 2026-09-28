package gr.grnet.pccapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.ServiceDto;
import gr.grnet.pccapi.endpoint.ServiceEndpoint;
import gr.grnet.pccapi.entity.Service;
import gr.grnet.pccapi.repository.ServiceRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestHTTPEndpoint(ServiceEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class ServiceEndpointTest {

  @Inject ServiceRepository serviceRepository;

  @KeycloakToken(username = "admin", password = "admin")
  String adminToken;

  private Integer serviceId;

  @BeforeEach
  @Transactional
  public void setUp() {

    var service = serviceRepository.findByName("SERVICE-ENDPOINT-TEST");

    if (service == null) {
      service = new Service();
      service.setName("SERVICE-ENDPOINT-TEST");
      serviceRepository.persist(service);
    }

    serviceId = service.id;
  }

  @Test
  public void listAllServices() {

    var response = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .get()
            .then()
            .statusCode(200)
            .extract()
            .as(ServiceDto[].class);

    assertTrue(
            java.util.Arrays.stream(response)
                    .anyMatch(service -> service.id.equals(serviceId) && service.name.equals("SERVICE-ENDPOINT-TEST")));
  }

  @Test
  public void listOneService() {

    var response = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .get("/{id}", serviceId)
            .then()
            .statusCode(200)
            .extract()
            .as(ServiceDto.class);

    assertEquals(serviceId, response.id);
    assertEquals("SERVICE-ENDPOINT-TEST", response.name);
  }

  @Test
  public void listOneServiceNotfound() {

    var response = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .get("/{id}", 999999)
            .then()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Service not found", response.getMessage());
  }
}