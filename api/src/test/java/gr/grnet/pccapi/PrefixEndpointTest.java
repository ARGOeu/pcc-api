package gr.grnet.pccapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import gr.grnet.pccapi.dto.*;
import gr.grnet.pccapi.endpoint.PrefixEndpoint;
import gr.grnet.pccapi.entity.Statistics;
import gr.grnet.pccapi.enums.ContractType;
import gr.grnet.pccapi.enums.LookUpServiceType;
import gr.grnet.pccapi.mapper.StatisticsMapper;
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
            .setLookUpServiceType(String.valueOf(LookUpServiceType.PRIVATE))
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1)
            .setContactEmail("test@test.com")
            .setContactName("testname")
            .setContractEnd("2008-01-01T00:00:00Z")
            .setContractType(ContractType.CONTRACT.name());

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
    assertEquals(String.valueOf(LookUpServiceType.PRIVATE), response.getLookUpServiceType());
    assertEquals(2, response.getStatus());
    assertEquals("Medical & Health Sciences", response.getDomainName());
    assertEquals(1, response.getDomainId());
    assertEquals("B2HANDLE", response.getServiceName());
    assertEquals(1, response.getServiceId());
    assertEquals("GRNET", response.getProviderName());
    assertEquals(1, response.getProviderId());
    assertEquals(Boolean.TRUE, response.getResolvable());
    assertEquals("test@test.com", response.getContactEmail());
    assertEquals("testname", response.getContactName());
    assertEquals("2008-01-01T00:00:00Z", response.getContractEnd());
    assertEquals(ContractType.CONTRACT.name(), response.getContractType());
  }

  @Test
  public void createPrefixWithNameAlreadyExists() {

    var requestBody =
        new PrefixDto()
            .setName("11527")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceType(String.valueOf(LookUpServiceType.PRIVATE))
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1)
            .setContractEnd("2008-01-01T00:00:00Z");

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
            .setLookUpServiceType(String.valueOf(LookUpServiceType.PRIVATE))
            .setContractEnd("2008-01-01T00:00:00Z");

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
            .setLookUpServiceType(String.valueOf(LookUpServiceType.PRIVATE))
            .setContractEnd("2008-01-01T00:00:00Z");

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
            .setLookUpServiceType(String.valueOf(LookUpServiceType.PRIVATE))
            .setContractEnd("2008-01-01T00:00:00Z");

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
            .setLookUpServiceType(String.valueOf(LookUpServiceType.NONE))
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1)
            .setResolvable(Boolean.TRUE)
            .setContractEnd("2008-01-01T00:00:00Z")
            .setContractType(ContractType.PROJECT.name());

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
            .setLookUpServiceType(String.valueOf(LookUpServiceType.PRIVATE))
            .setDomainId(2)
            .setServiceId(2)
            .setProviderId(2)
            .setResolvable(Boolean.FALSE)
            .setContactEmail("test2@test.com")
            .setContactName("testname2")
            .setContractEnd("2018-01-01T00:00:00Z")
            .setContractType(ContractType.OTHER.name());

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
    assertEquals(String.valueOf(LookUpServiceType.PRIVATE), response.getLookUpServiceType());

    assertEquals(3, response.getStatus());
    assertEquals("77777", response.getName());
    assertEquals(Boolean.FALSE, response.getResolvable());
    assertEquals("testname2", response.getContactName());
    assertEquals("test2@test.com", response.getContactEmail());
    assertEquals("2018-01-01T00:00:00Z", response.getContractEnd());
    assertEquals(ContractType.OTHER.name(), response.getContractType());
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
            .setLookUpServiceType(String.valueOf(LookUpServiceType.PRIVATE))
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1)
            .setContractEnd("2008-01-01T00:00:00Z");

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
            .setLookUpServiceType(String.valueOf(LookUpServiceType.PRIVATE))
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1)
            .setContractEnd("2008-01-01T00:00:00Z");

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
            .setLookUpServiceType(String.valueOf(LookUpServiceType.PRIVATE))
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1)
            .setContractEnd("2008-01-01T00:00:00Z");

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
            .setLookUpServiceType(String.valueOf(LookUpServiceType.BOTH))
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1)
            .setContractEnd("2008-01-01T00:00:00Z");

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
    assertEquals("2008-01-01T00:00:00Z", prefixResponseDto.contractEnd);
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
  public void fetchResolvablePIDCountByPrefixId() throws SQLException {

    Mockito.when(statisticsService.getResolvablePIDCountByPrefixID(any())).thenReturn(21);
    var resp =
        given()
            .get("/{id}/resolvable", 21.12132)
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(Integer.class);

    assertEquals(21, resp);
  }

  @Test
  public void fetchResolvablePIDCountByPrefixIdNotFound() {

    Mockito.when(statisticsService.getResolvablePIDCountByPrefixID("invalid"))
        .thenThrow(new NotFoundException("Prefix invalid not found"));
    var resp =
        given()
            .get("/{id}/resolvable", "invalid")
            .then()
            .assertThat()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Prefix invalid not found", resp.getMessage());
  }

  @Test
  public void fetchStatisticsByPrefixId() throws SQLException {

    Mockito.when(statisticsService.getPrefixStatisticsByID(any()))
        .thenReturn(
            StatisticsMapper.INSTANCE.statisticsToDto(new Statistics("21.12132", 2, 3, 4, 5)));
    var resp =
        given()
            .get("/{id}/statistics", 21.12132)
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(StatisticsDto.class);
    assertEquals(resp.prefix, "21.12132");
    assertEquals(resp.handlesCount, 2);
    assertEquals(resp.resolvableCount, 3);
    assertEquals(resp.unresolvableCount, 4);
    assertEquals(resp.uncheckedCount, 5);
  }

  @Test
  public void testPartiallyUpdatePrefix() {

    var postRequestBody =
        new PrefixDto()
            .setName("212121")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceType(String.valueOf(LookUpServiceType.CENTRAL))
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1)
            .setResolvable(Boolean.TRUE)
            .setContactName("testname")
            .setContactEmail("test@test.com")
            .setContractEnd("2008-01-01T00:00:00Z")
            .setContractType(ContractType.OTHER.name());

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
            .setLookUpServiceType(String.valueOf(LookUpServiceType.PRIVATE))
            .setDomainId(2)
            .setResolvable(Boolean.FALSE)
            .setContactEmail("test2@test.com")
            .setContactName("testname2")
            .setContractEnd("2018-01-01T00:00:00Z")
            .setContractType(ContractType.PROJECT.name());
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
    assertEquals(patchRequestBody.contactEmail, patchResponse.contactEmail);
    assertEquals(patchRequestBody.contactName, patchResponse.contactName);
    assertEquals(patchRequestBody.contractEnd, patchResponse.contractEnd);
    assertEquals(ContractType.PROJECT.name(), patchResponse.getContractType());
  }

  @Test
  public void testPartiallyUpdatePrefixEmptyField() {

    var postRequestBody =
        new PrefixDto()
            .setName("212121")
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceType(String.valueOf(LookUpServiceType.PRIVATE))
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1)
            .setContractEnd("2018-01-01T00:00:00Z");

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
            .setLookUpServiceType(String.valueOf(LookUpServiceType.PRIVATE))
            .setDomainId(1)
            .setServiceId(1)
            .setProviderId(1)
            .setContractEnd("2018-01-01T00:00:00Z");

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

  @Test
  public void testPrefixStatistics() {
    var statisticsDto = new StatisticsDto();
    statisticsDto.prefix = "test";
    statisticsDto.handlesCount = 10;
    statisticsDto.resolvableCount = 1;
    statisticsDto.unresolvableCount = 1;
    statisticsDto.uncheckedCount = 8;

    // Mockito.when(mockedPrefixRepository.existsByName(any())).thenReturn(true);
    // Mockito.when(mockedPrefixRepository.existsByName(any())).thenReturn(true);
    Mockito.when(statisticsService.setPrefixStatistics(any(), any())).thenReturn(statisticsDto);

    var dto =
        new StatisticsRequestDto()
            .setHandlesCount(10)
            .setResolvableCount(1)
            .setUnresolvableCount(1)
            .setUncheckedCount(8);
    var resp =
        given()
            .body(dto)
            .contentType(ContentType.JSON)
            .post("/{id}/statistics", "test")
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(StatisticsDto.class);

    assertEquals(resp.handlesCount, dto.handlesCount);
    assertEquals(statisticsDto.prefix, statisticsDto.prefix);
    assertEquals(resp.resolvableCount, dto.resolvableCount);
    assertEquals(resp.uncheckedCount, dto.uncheckedCount);
  }
}
