package gr.grnet.pccapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gr.grnet.pccapi.client.eoscportal.EOSCPortalClient;
import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.DomainDto;
import gr.grnet.pccapi.endpoint.DomainEndpoint;
import gr.grnet.pccapi.repository.DomainRepository;
import gr.grnet.pccapi.service.DomainService;
import gr.grnet.pccapi.wiremock.EOSCPortalDomainWiremockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@QuarkusTestResource(EOSCPortalDomainWiremockServer.class)
@TestProfile(PCCApiTestProfile.class)
@TestHTTPEndpoint(DomainEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DomainEndpointTest {

  @Inject @RestClient EOSCPortalClient eoscPortalClient;

  @Inject DomainService domainService;

  @Inject DomainRepository domainRepository;

  @BeforeAll
  public void setup() throws ExecutionException, InterruptedException {

    var eoscPortalDomains =
        eoscPortalClient.getByType("SCIENTIFIC_DOMAIN").toCompletableFuture().get();

    domainService.saveEoscPortalDomains(eoscPortalDomains);
  }

  @Test
  public void fetchDomainById() {

    var domain = domainRepository.findById(1);

    var domainDto =
        given().get("/{id}", 1).then().assertThat().statusCode(200).extract().as(DomainDto.class);

    assertEquals(domainDto.name, domain.name);
    assertEquals(domainDto.domainId, domain.domainId);
    assertEquals(domainDto.description, domain.description);
  }

  @Test
  public void fetchDomainByIdNotFound() {

    var response =
        given()
            .get("/{id}", 999)
            .then()
            .assertThat()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Domain not found", response.getMessage());
  }

  @Test
  public void fetchAllDomains() {

    var domainDtos =
        given().get().then().assertThat().statusCode(200).extract().as(DomainDto[].class);

    assertEquals(8, domainDtos.length);
  }
}
