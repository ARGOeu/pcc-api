package gr.grnet.pccapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.PartialPrefixDto;
import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.dto.PrefixResponseDto;
import gr.grnet.pccapi.endpoint.PrefixEndpoint;
import gr.grnet.pccapi.enums.LookUpServiceType;
import gr.grnet.pccapi.repository.DomainRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.repository.ServiceRepository;
import gr.grnet.pccapi.service.StatisticsService;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestHTTPEndpoint(PrefixEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PrefixEndpointTest {

  @Inject PrefixRepository prefixRepository;
  @Inject DomainRepository domainRepository;
  @Inject ProviderRepository providerRepository;
  @Inject ServiceRepository serviceRepository;
  @InjectMock StatisticsService statisticsService;

  @BeforeEach
  @Transactional
  public void cleanDB() {
    prefixRepository.deleteAll();
  }

  @Test
  public void createPrefix() {

    var requestBody =
        new PrefixDto()
            .setName("11523")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceType(LookUpServiceType.PRIVATE)
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1);

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .as(PrefixResponseDto.class);

    assertEquals(prefixRepository.find("name", "11523").firstResult().id, response.getId());
    assertEquals("11523", response.getName());
    assertEquals("someone", response.getOwner());
    assertEquals("someone else", response.getUsedBy());
    assertEquals(LookUpServiceType.PRIVATE, response.getLookUpServiceType());
    assertEquals(2, response.getStatus());
    assertEquals("Medical & Health Sciences", response.getDomainName());
    assertEquals(1, response.getDomainId());
    assertEquals("B2HANDLE", response.getServiceName());
    assertEquals(1, response.getServiceId());
    assertEquals("GRNET", response.getProviderName());
    assertEquals(1, response.getProviderId());
    assertEquals(Boolean.TRUE, response.getResolvable());
  }

  @Test
  public void createPrefixWithNameAlreadyExists() {

    var requestBody =
        new PrefixDto()
            .setName("11527")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceType(LookUpServiceType.PRIVATE)
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1);

    // create the prefix
    given().contentType(ContentType.JSON).body(requestBody).post();

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .assertThat()
            .statusCode(409)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Prefix name already exists", response.getMessage());
  }

  @Test
  public void createPrefixWithInvalidService() {

    var requestBody =
        new PrefixDto()
            .setName("11524")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setDomainId(1)
            .setServiceId(999)
            .setProviderId(1)
            .setLookUpServiceType(LookUpServiceType.PRIVATE);

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .assertThat()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Service not found", response.getMessage());
  }

  @Test
  public void createPrefixWithInvalidDomain() {

    var requestBody =
        new PrefixDto()
            .setName("11524")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setDomainId(999)
            .setServiceId(1)
            .setProviderId(1)
            .setLookUpServiceType(LookUpServiceType.PRIVATE);

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .assertThat()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Domain not found", response.getMessage());
  }

  @Test
  public void createPrefixWithInvalidProvider() {

    var requestBody =
        new PrefixDto()
            .setName("11524")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(999)
            .setLookUpServiceType(LookUpServiceType.PRIVATE);

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .assertThat()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Provider not found", response.getMessage());
  }

  @Test
  public void testUpdate() {
    var requestBody =
        new PrefixDto()
            .setName("666666")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceType(LookUpServiceType.NONE)
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1)
            .setResolvable(Boolean.TRUE);

    var resp =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .as(PrefixResponseDto.class);

    var updateRequestDto =
        new PrefixDto()
            .setName("77777")
            .setOwner("someone1")
            .setStatus(3)
            .setUsedBy("someone else1")
            .setLookUpServiceType(LookUpServiceType.PRIVATE)
            .setDomainId(2)
            .setServiceId(2)
            .setProviderId(2)
            .setResolvable(Boolean.FALSE);

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(updateRequestDto)
            .put(String.valueOf(resp.id))
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(PrefixResponseDto.class);

    assertEquals(2, response.getDomainId());
    assertEquals(2, response.getProviderId());
    assertEquals(2, response.getServiceId());

    assertEquals("someone1", response.getOwner());
    assertEquals("someone else1", response.getUsedBy());
    assertEquals(LookUpServiceType.PRIVATE, response.getLookUpServiceType());

    assertEquals(3, response.getStatus());
    assertEquals("77777", response.getName());
    assertEquals(Boolean.FALSE, response.getResolvable());
  }

  @Test
  public void updatePrefixNotfound() {

    // creating a new Prefix
    var requestBody =
        new PrefixDto()
            .setName("11545")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceType(LookUpServiceType.PRIVATE)
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1);

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .as(PrefixResponseDto.class);

    var resp =
        given()
            .put("/{id}", response.id + 10)
            .then()
            .assertThat()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Prefix not found", resp.getMessage());
  }

  @Test
  public void deletePrefix() {

    // creating a new Prefix
    var requestBody =
        new PrefixDto()
            .setName("11545")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceType(LookUpServiceType.PRIVATE)
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1);

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .as(PrefixResponseDto.class);

    // deleting an existing Prefix

    given().delete("/{id}", response.id).then().assertThat().statusCode(200);
  }

  @Test
  public void deletePrefixNotfound() {

    // creating a new Prefix
    var requestBody =
        new PrefixDto()
            .setName("11545")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceType(LookUpServiceType.PRIVATE)
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1);

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .as(PrefixResponseDto.class);

    // deleting an existing Prefix

    var resp =
        given()
            .delete("/{id}", response.id + 10)
            .then()
            .assertThat()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Prefix not found", resp.getMessage());
  }

  @Test
  public void fetchPrefixById() {

    var requestBody =
        new PrefixDto()
            .setName("12345")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceType(LookUpServiceType.BOTH)
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1);

    var created =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .as(PrefixResponseDto.class);

    var prefixResponseDto =
        given()
            .get("/{id}", created.id)
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(PrefixResponseDto.class);

    assertEquals(created.name, prefixResponseDto.name);
    assertEquals(created.domainId, prefixResponseDto.domainId);
    assertEquals(created.id, prefixResponseDto.id);
    assertEquals(created.lookUpServiceType, prefixResponseDto.lookUpServiceType);
  }

  @Test
  public void fetchPrefixByIdNotFound() {
    var resp =
        given()
            .get("/{id}", 999)
            .then()
            .assertThat()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Prefix not found", resp.getMessage());
  }

  @Test
  public void testFetchAllPrefixes() {

    var prefixes = prefixRepository.findAll().list();

    var prefixResponseDto =
        given().get().then().assertThat().statusCode(200).extract().as(PrefixResponseDto[].class);

    assertEquals(prefixes.size(), prefixResponseDto.length);
  }

  @Test
  public void fetchHandlesCountByPrefixId() throws SQLException {

    Mockito.when(statisticsService.getPIDCountByPrefixID(any())).thenReturn(21);
    var resp =
        given()
            .get("/{id}/count", 21.12132)
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(Integer.class);

    assertEquals(21, resp);
  }

  @Test
  public void fetchHandlesCountByPrefixIdNotFound() {

    Mockito.when(statisticsService.getPIDCountByPrefixID("invalid"))
        .thenThrow(new NotFoundException("Prefix invalid not found"));
    var resp =
        given()
            .get("/{id}/count", "invalid")
            .then()
            .assertThat()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Prefix invalid not found", resp.getMessage());
  }

  @Test
  public void testPartiallyUpdatePrefix() {

    var postRequestBody =
        new PrefixDto()
            .setName("212121")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceType(LookUpServiceType.CENTRAL)
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1)
            .setResolvable(Boolean.TRUE);

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(postRequestBody)
            .post()
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .as(PrefixResponseDto.class);

    var patchRequestBody =
        new PartialPrefixDto()
            .setName("222222")
            .setLookUpServiceType(LookUpServiceType.PRIVATE)
            .setDomainId(2)
            .setResolvable(Boolean.FALSE);

    var patchResponse =
        given()
            .contentType(ContentType.JSON)
            .body(patchRequestBody)
            .patch("/{id}", response.id)
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(PrefixResponseDto.class);

    assertEquals(patchRequestBody.name, patchResponse.name);
    assertEquals(patchRequestBody.domainId, patchResponse.domainId);
    assertEquals(patchRequestBody.lookUpServiceType, patchResponse.lookUpServiceType);
    assertEquals(patchRequestBody.resolvable, patchResponse.resolvable);
  }

  @Test
  public void testPartiallyUpdatePrefixEmptyField() {

    var postRequestBody =
        new PrefixDto()
            .setName("212121")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceType(LookUpServiceType.PRIVATE)
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1);

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(postRequestBody)
            .post()
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .as(PrefixResponseDto.class);

    var patchRequestBody = new PartialPrefixDto().setName("").setDomainId(2);

    var patchResponse =
        given()
            .contentType(ContentType.JSON)
            .body(patchRequestBody)
            .patch("/{id}", response.id)
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(PrefixResponseDto.class);

    assertEquals(postRequestBody.name, patchResponse.name);
    assertEquals(patchRequestBody.domainId, patchResponse.domainId);
  }

  @Test
  public void testPartiallyUpdatePrefixIncorrectDomain() {

    var postRequestBody =
        new PrefixDto()
            .setName("232323")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceType(LookUpServiceType.PRIVATE)
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1);

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(postRequestBody)
            .post()
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .as(PrefixResponseDto.class);

    var patchRequestBody = new PartialPrefixDto().setName("222222").setDomainId(999);

    var patchResponse =
        given()
            .contentType(ContentType.JSON)
            .body(patchRequestBody)
            .patch("/{id}", response.id)
            .then()
            .assertThat()
            .statusCode(404);
  }
}
