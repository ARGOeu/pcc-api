package gr.grnet.pccapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.pagination.PageResource;
import gr.grnet.pccapi.dto.prefix.PartialPrefixDto;
import gr.grnet.pccapi.dto.prefix.PrefixRequestDto;
import gr.grnet.pccapi.dto.prefix.PrefixResponseDto;
import gr.grnet.pccapi.dto.statistic.StatisticsDto;
import gr.grnet.pccapi.dto.statistic.StatisticsRequestDto;
import gr.grnet.pccapi.endpoint.PrefixEndpoint;
import gr.grnet.pccapi.entity.Statistics;
import gr.grnet.pccapi.mapper.StatisticsMapper;
import gr.grnet.pccapi.repository.PrefixRepository;
import gr.grnet.pccapi.repository.ServiceRepository;
import gr.grnet.pccapi.service.StatisticsService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@TestHTTPEndpoint(PrefixEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class PrefixEndpointTest {

  @Inject PrefixRepository prefixRepository;
  @Inject ServiceRepository serviceRepository;
  @InjectMock StatisticsService statisticsService;

  @KeycloakToken(username = "admin", password = "admin")
  String adminToken;

  @BeforeEach
  @Transactional
  public void cleanDB() {
    prefixRepository.deleteAll();
  }

  // ---------------------------------------------------------------------------
  // CREATE
  // ---------------------------------------------------------------------------

  @Test
  public void createPrefix() {

    var requestBody = validPrefixRequest("11523")
            .setContractTypeId(6);

    var response = createPrefix(requestBody);

    var service = serviceRepository.findByName("NEWSERVICE");

    assertNotNull(service);
    assertEquals(service.id, response.getServiceId());
    assertEquals(prefixRepository.find("name", "11523").firstResult().id, response.getId());
    assertEquals("11523", response.getName());
    assertEquals("someone", response.getOwner());
    assertEquals("someone else", response.getUsedBy());
    assertEquals(2, response.getLookUpServiceTypeId());
    assertEquals(2, response.getStatus());
    assertEquals(1, response.getDomainId());
    assertEquals("NEWSERVICE", response.getServiceName());
    assertEquals("GRNET", response.getProviderName());
    assertEquals(1, response.getProviderId());
    assertEquals(Boolean.TRUE, response.getResolvable());
    assertEquals("test@test.com", response.getContactEmail());
    assertEquals("testname", response.getContactName());
    assertEquals("2008-01-01T00:00:00Z", response.getContractEnd());
    assertEquals(6, response.getContractTypeId());
  }

  @Test
  public void createPrefixCreatesNewService() {

    var requestBody = validPrefixRequest("new-service-prefix")
            .setServiceName("MY NEW SERVICE");

    var response = createPrefix(requestBody);

    var service = serviceRepository.findByName("MY NEW SERVICE");

    assertNotNull(service);
    assertEquals(service.id, response.getServiceId());
    assertEquals("MY NEW SERVICE", response.getServiceName());
  }

  @Test
  public void createPrefixReusesExistingServiceCaseInsensitive() {

    var first = createPrefix(validPrefixRequest("prefix-one")
            .setServiceName("My Service"));

    var second = createPrefix(validPrefixRequest("prefix-two")
            .setServiceName("my service"));

    assertEquals(first.getServiceId(), second.getServiceId());

    var service = serviceRepository.findByName("MY SERVICE");

    assertNotNull(service);
    assertEquals(first.getServiceId(), service.id);
  }

  @Test
  public void createPrefixWithoutService() {

    var response = createPrefix(
            validPrefixRequest("without-service")
                    .setServiceName(null));

    assertNull(response.getServiceId());
    assertNull(response.getServiceName());
  }

  @Test
  public void createPrefixWithNameAlreadyExists() {

    var requestBody = validPrefixRequest("11527");

    createPrefix(requestBody);

    var response = authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .statusCode(409)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Prefix name already exists", response.getMessage());
  }

  @Test
  public void createPrefixWithInvalidDomain() {

    var requestBody = validPrefixRequest("invalid-domain")
            .setDomainId(999);

    var response = authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Domain not found", response.getMessage());
  }

  @Test
  public void createPrefixWithInvalidProvider() {

    var requestBody = validPrefixRequest("invalid-provider")
            .setProviderId(999);

    var response = authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Provider not found", response.getMessage());
  }

  // ---------------------------------------------------------------------------
  // PUT
  // ---------------------------------------------------------------------------

  @Test
  public void testUpdate() {

    var created = createPrefix(
            validPrefixRequest("666666")
                    .setResolvable(Boolean.TRUE));

    var updateRequestDto = validPrefixRequest("77777")
            .setOwner("someone1")
            .setStatus(3)
            .setUsedBy("someone else1")
            .setDomainId(2)
            .setServiceName("UPDATEDSERVICE")
            .setProviderId(3)
            .setResolvable(Boolean.FALSE)
            .setContactEmail("test2@test.com")
            .setContactName("testname2")
            .setContractEnd("2018-01-01")
            .setContractTypeId(7);

    var response = authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(updateRequestDto)
            .put("/{id}", created.getId())
            .then()
            .statusCode(200)
            .extract()
            .as(PrefixResponseDto.class);

    var service = serviceRepository.findByName("UPDATEDSERVICE");

    assertNotNull(service);
    assertEquals(2, response.getDomainId());
    assertEquals(3, response.getProviderId());
    assertEquals(service.id, response.getServiceId());
    assertEquals("UPDATEDSERVICE", response.getServiceName());
    assertEquals("someone1", response.getOwner());
    assertEquals("someone else1", response.getUsedBy());
    assertEquals(2, response.getLookUpServiceTypeId());
    assertEquals(3, response.getStatus());
    assertEquals("77777", response.getName());
    assertEquals(Boolean.FALSE, response.getResolvable());
    assertEquals("testname2", response.getContactName());
    assertEquals("test2@test.com", response.getContactEmail());
    assertEquals("2018-01-01T00:00:00Z", response.getContractEnd());
    assertEquals(7, response.getContractTypeId());
  }

  @Test
  public void updatePrefixNotFound() {

    var response = authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(validPrefixRequest("not-found"))
            .put("/{id}", 999)
            .then()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Prefix not found", response.getMessage());
  }

  @Test
  public void updatePrefixWithoutStatusAndOptionalTypes() {

    var created = createPrefix(
            validPrefixRequest("put-optional"));

    var updateRequestBody = new PrefixRequestDto()
            .setName("put-optional-updated")
            .setOwner("someone updated")
            .setDomainId(1)
            .setServiceName("NEWSERVICE")
            .setProviderId(1)
            .setContactEmail("updated@test.com")
            .setContactName("updated");

    var response = authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(updateRequestBody)
            .put("/{id}", created.getId())
            .then()
            .statusCode(200)
            .extract()
            .as(PrefixResponseDto.class);

    assertEquals("put-optional-updated", response.getName());
    assertNull(response.getStatus());
    assertNull(response.getContractTypeId());
    assertNull(response.getLookUpServiceTypeId());
  }

  // ---------------------------------------------------------------------------
  // PATCH
  // ---------------------------------------------------------------------------

  @Test
  public void testPartiallyUpdatePrefix() {

    var created = createPrefix(
            validPrefixRequest("212121")
                    .setServiceName("ORIGINALSERVICE"));

    var patchRequestBody = new PartialPrefixDto()
            .setName("222222")
            .setLookUpServiceTypeId(2)
            .setContractTypeId(5)
            .setDomainId(2)
            .setResolvable(Boolean.FALSE)
            .setContactEmail("test2@test.com")
            .setContactName("testname2")
            .setContractEnd("2018-01-01");

    var patchResponse = authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(patchRequestBody)
            .patch("/{id}", created.getId())
            .then()
            .statusCode(200)
            .extract()
            .as(PrefixResponseDto.class);

    assertEquals(patchRequestBody.getName(), patchResponse.getName());
    assertEquals(patchRequestBody.getDomainId(), patchResponse.getDomainId());
    assertEquals(patchRequestBody.getLookUpServiceTypeId(), patchResponse.getLookUpServiceTypeId());
    assertEquals(patchRequestBody.getResolvable(), patchResponse.getResolvable());
    assertEquals(patchRequestBody.getContactEmail(), patchResponse.getContactEmail());
    assertEquals(patchRequestBody.getContactName(), patchResponse.getContactName());
    assertEquals("2018-01-01T00:00:00Z", patchResponse.getContractEnd());
    assertEquals(5, patchResponse.getContractTypeId());
    assertEquals(created.getServiceId(), patchResponse.getServiceId());
    assertEquals("ORIGINALSERVICE", patchResponse.getServiceName());
  }

  @Test
  public void patchPrefixUpdatesService() {

    var created = createPrefix(
            validPrefixRequest("patch-service")
                    .setServiceName("OLD SERVICE"));

    var patchRequestBody = new PartialPrefixDto()
            .setServiceName("NEW SERVICE");

    var response = authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(patchRequestBody)
            .patch("/{id}", created.getId())
            .then()
            .statusCode(200)
            .extract()
            .as(PrefixResponseDto.class);

    var service = serviceRepository.findByName("NEW SERVICE");

    assertNotNull(service);
    assertEquals(service.id, response.getServiceId());
    assertEquals("NEW SERVICE", response.getServiceName());
  }

  @Test
  public void patchPrefixKeepsServiceWhenServiceNotProvided() {

    var created = createPrefix(
            validPrefixRequest("keep-service")
                    .setServiceName("KEEP THIS SERVICE"));

    var patchRequestBody = new PartialPrefixDto()
            .setOwner("updated owner");

    var response = authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(patchRequestBody)
            .patch("/{id}", created.getId())
            .then()
            .statusCode(200)
            .extract()
            .as(PrefixResponseDto.class);

    assertEquals("updated owner", response.getOwner());
    assertEquals(created.getServiceId(), response.getServiceId());
    assertEquals("KEEP THIS SERVICE", response.getServiceName());
  }

  @Test
  public void testPartiallyUpdatePrefixEmptyField() {

    var created = createPrefix(
            validPrefixRequest("212121")
                    .setServiceName("SERVICETESTNAME"));

    var patchRequestBody = new PartialPrefixDto()
            .setName("")
            .setDomainId(2);

    var response = authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(patchRequestBody)
            .patch("/{id}", created.getId())
            .then()
            .statusCode(200)
            .extract()
            .as(PrefixResponseDto.class);

    assertEquals("212121", response.getName());
    assertEquals(2, response.getDomainId());
    assertEquals(created.getServiceId(), response.getServiceId());
    assertEquals("SERVICETESTNAME", response.getServiceName());
  }

  @Test
  public void testPartiallyUpdatePrefixIncorrectDomain() {

    var created = createPrefix(validPrefixRequest("232323"));

    var patchRequestBody = new PartialPrefixDto()
            .setName("222222")
            .setDomainId(999);

    authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(patchRequestBody)
            .patch("/{id}", created.getId())
            .then()
            .statusCode(404);
  }

  // ---------------------------------------------------------------------------
  // FETCH
  // ---------------------------------------------------------------------------

  @Test
  public void fetchPrefixById() {

    var created = createPrefix(validPrefixRequest("12345"));

    var response = authenticatedRequest()
            .get("/{id}", created.getId())
            .then()
            .statusCode(200)
            .extract()
            .as(PrefixResponseDto.class);

    assertEquals(created.getName(), response.getName());
    assertEquals(created.getDomainId(), response.getDomainId());
    assertEquals(created.getId(), response.getId());
    assertEquals(created.getLookUpServiceTypeId(), response.getLookUpServiceTypeId());
    assertEquals(created.getServiceId(), response.getServiceId());
    assertEquals(created.getServiceName(), response.getServiceName());
    assertEquals("2008-01-01T00:00:00Z", response.getContractEnd());
  }

  @Test
  public void fetchPrefixByIdNotFound() {

    var response = authenticatedRequest()
            .get("/{id}", 999)
            .then()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Prefix not found", response.getMessage());
  }

  @Test
  public void testFetchPrefixesByPage() {

    createPrefix(validPrefixRequest("prefix-one"));
    createPrefix(validPrefixRequest("prefix-two"));

    var response = authenticatedRequest()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .as(PageResource.class);

    assertEquals(2, response.getTotalElements());
  }

  // ---------------------------------------------------------------------------
  // SEARCH / FILTERING
  // ---------------------------------------------------------------------------

  @Test
  public void fetchPrefixesBySearch() {

    createPrefix(
            validPrefixRequest("21.SEARCH-ME")
                    .setServiceName("SERVICE-A"));

    createPrefix(
            validPrefixRequest("21.OTHER")
                    .setServiceName("SERVICE-B"));

    var response = authenticatedRequest()
            .queryParam("search", "SEARCH-ME")
            .get()
            .then()
            .statusCode(200)
            .extract()
            .jsonPath();

    assertEquals(1, response.getInt("total_elements"));
    assertEquals("21.SEARCH-ME", response.getString("content[0].name"));
  }

  @Test
  public void fetchPrefixesFilteredByProvider() {

    var grnet = createPrefix(
            validPrefixRequest("provider-grnet")
                    .setProviderId(1)
                    .setServiceName("SERVICE-A"));

    createPrefix(
            validPrefixRequest("provider-surf")
                    .setProviderId(3)
                    .setServiceName("SERVICE-B"));

    var response = authenticatedRequest()
            .queryParam("provider", grnet.getProviderName())
            .get()
            .then()
            .statusCode(200)
            .extract()
            .jsonPath();

    assertEquals(1, response.getInt("total_elements"));
    assertEquals("provider-grnet", response.getString("content[0].name"));
    assertEquals(grnet.getProviderName(), response.getString("content[0].provider_name"));
  }

  @Test
  public void fetchPrefixesFilteredByDomain() {

    var lifeSciences = createPrefix(
            validPrefixRequest("domain-life")
                    .setDomainId(1)
                    .setServiceName("SERVICE-A"));

    createPrefix(
            validPrefixRequest("domain-physical")
                    .setDomainId(2)
                    .setServiceName("SERVICE-B"));

    var response = authenticatedRequest()
            .queryParam("domain", lifeSciences.getDomainName())
            .get()
            .then()
            .statusCode(200)
            .extract()
            .jsonPath();

    assertEquals(1, response.getInt("total_elements"));
    assertEquals("domain-life", response.getString("content[0].name"));
    assertEquals(lifeSciences.getDomainName(), response.getString("content[0].domain_name"));
  }

  @Test
  public void fetchPrefixesFilteredByContractType() {

    var first = createPrefix(
            validPrefixRequest("contract-one")
                    .setContractTypeId(5)
                    .setServiceName("SERVICE-A"));

    createPrefix(
            validPrefixRequest("contract-two")
                    .setContractTypeId(6)
                    .setServiceName("SERVICE-B"));

    var response = authenticatedRequest()
            .queryParam("contract_type", first.getContractTypeName())
            .get()
            .then()
            .statusCode(200)
            .extract()
            .jsonPath();

    assertEquals(1, response.getInt("total_elements"));
    assertEquals("contract-one", response.getString("content[0].name"));
    assertEquals(first.getContractTypeName(), response.getString("content[0].contract_type_name"));
  }

  @Test
  public void fetchPrefixesFilteredByProviderDomainAndContractType() {

    var target = createPrefix(
            validPrefixRequest("target-prefix")
                    .setProviderId(1)
                    .setDomainId(1)
                    .setContractTypeId(5)
                    .setServiceName("TARGET-SERVICE"));

    createPrefix(
            validPrefixRequest("different-provider")
                    .setProviderId(3)
                    .setDomainId(1)
                    .setContractTypeId(5)
                    .setServiceName("SERVICE-A"));

    createPrefix(
            validPrefixRequest("different-domain")
                    .setProviderId(1)
                    .setDomainId(2)
                    .setContractTypeId(5)
                    .setServiceName("SERVICE-B"));

    createPrefix(
            validPrefixRequest("different-contract")
                    .setProviderId(1)
                    .setDomainId(1)
                    .setContractTypeId(6)
                    .setServiceName("SERVICE-C"));

    var response = authenticatedRequest()
            .queryParam("provider", target.getProviderName())
            .queryParam("domain", target.getDomainName())
            .queryParam("contract_type", target.getContractTypeName())
            .get()
            .then()
            .statusCode(200)
            .extract()
            .jsonPath();

    assertEquals(1, response.getInt("total_elements"));
    assertEquals("target-prefix", response.getString("content[0].name"));
  }

  @Test
  public void fetchPrefixesWithSearchAndFilters() {

    var target = createPrefix(
            validPrefixRequest("21.MATCH")
                    .setProviderId(1)
                    .setDomainId(1)
                    .setContractTypeId(5)
                    .setServiceName("MATCH-SERVICE"));

    createPrefix(
            validPrefixRequest("21.OTHER")
                    .setProviderId(3)
                    .setDomainId(2)
                    .setContractTypeId(6)
                    .setServiceName("OTHER-SERVICE"));

    var response = authenticatedRequest()
            .queryParam("search", "21.MATCH")
            .queryParam("provider", target.getProviderName())
            .queryParam("domain", target.getDomainName())
            .queryParam("contract_type", target.getContractTypeName())
            .get()
            .then()
            .statusCode(200)
            .extract()
            .jsonPath();

    assertEquals(1, response.getInt("total_elements"));
    assertEquals("21.MATCH", response.getString("content[0].name"));
  }

  // ---------------------------------------------------------------------------
  // DELETE
  // ---------------------------------------------------------------------------

  @Test
  public void deletePrefix() {

    var created = createPrefix(validPrefixRequest("delete-prefix"));

    authenticatedRequest()
            .delete("/{id}", created.getId())
            .then()
            .statusCode(200);

    assertNull(prefixRepository.findById(created.getId()));
  }

  @Test
  public void deletePrefixNotFound() {

    var response = authenticatedRequest()
            .delete("/{id}", 999)
            .then()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Prefix not found", response.getMessage());
  }

  // ---------------------------------------------------------------------------
  // STATISTICS
  // ---------------------------------------------------------------------------

  @Test
  public void fetchHandlesCountByPrefixIdNotFound() {

    Mockito.when(statisticsService.getPIDCountByPrefixID("invalid"))
            .thenThrow(new NotFoundException("Prefix invalid not found"));

    var response = authenticatedRequest()
            .get("/{id}/count", "invalid")
            .then()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Prefix invalid not found", response.getMessage());
  }

  @Test
  public void fetchResolvablePIDCountByPrefixIdNotFound() {

    Mockito.when(statisticsService.getResolvablePIDCountByPrefixID("invalid"))
            .thenThrow(new NotFoundException("Prefix invalid not found"));

    var response = authenticatedRequest()
            .get("/{id}/resolvable", "invalid")
            .then()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Prefix invalid not found", response.getMessage());
  }

  @Test
  public void fetchStatisticsByPrefixId() {

    Mockito.when(statisticsService.getPrefixStatisticsByID(any()))
            .thenReturn(StatisticsMapper.INSTANCE.statisticsToDto(
                    new Statistics("21.12132", 2, 3, 4, 5)));

    var response = authenticatedRequest()
            .get("/{id}/statistic", "21.12132")
            .then()
            .statusCode(200)
            .extract()
            .as(StatisticsDto.class);

    assertEquals("21.12132", response.prefix);
    assertEquals(2, response.handlesCount);
    assertEquals(3, response.resolvableCount);
    assertEquals(4, response.unresolvableCount);
    assertEquals(5, response.uncheckedCount);
  }

  @Test
  public void testPrefixStatistics() {

    var statisticsDto = new StatisticsDto();
    statisticsDto.prefix = "test";
    statisticsDto.handlesCount = 10;
    statisticsDto.resolvableCount = 1;
    statisticsDto.unresolvableCount = 1;
    statisticsDto.uncheckedCount = 8;

    Mockito.when(statisticsService.setPrefixStatistics(any(), any()))
            .thenReturn(statisticsDto);

    var request = new StatisticsRequestDto()
            .setHandlesCount(10)
            .setResolvableCount(1)
            .setUnresolvableCount(1)
            .setUncheckedCount(8);

    var response = authenticatedRequest()
            .body(request)
            .contentType(ContentType.JSON)
            .post("/{id}/statistic", "test")
            .then()
            .statusCode(200)
            .extract()
            .as(StatisticsDto.class);

    assertEquals("test", response.prefix);
    assertEquals(request.handlesCount, response.handlesCount);
    assertEquals(request.resolvableCount, response.resolvableCount);
    assertEquals(request.unresolvableCount, response.unresolvableCount);
    assertEquals(request.uncheckedCount, response.uncheckedCount);
  }

  // ---------------------------------------------------------------------------
  // HELPERS
  // ---------------------------------------------------------------------------

  private PrefixRequestDto validPrefixRequest(String name) {

    return new PrefixRequestDto()
            .setName(name)
            .setOwner("someone")
            .setStatus(2)
            .setUsedBy("someone else")
            .setLookUpServiceTypeId(2)
            .setContractTypeId(5)
            .setDomainId(1)
            .setServiceName("NEWSERVICE")
            .setProviderId(1)
            .setResolvable(Boolean.TRUE)
            .setContactName("testname")
            .setContactEmail("test@test.com")
            .setContractEnd("2008-01-01");
  }

  private PrefixResponseDto createPrefix(PrefixRequestDto requestBody) {

    return authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post()
            .then()
            .statusCode(201)
            .extract()
            .as(PrefixResponseDto.class);
  }

  private RequestSpecification authenticatedRequest() {

    return given()
            .header("Authorization", "Bearer " + adminToken);
  }
}