package gr.grnet.pccapi;

import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.dto.PrefixResponseDto;
import gr.grnet.pccapi.endpoint.PrefixEndpoint;
import gr.grnet.pccapi.exception.APIError;
import gr.grnet.pccapi.repository.PrefixRepository;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(PrefixEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PrefixEndpointTest {

    @Inject
    PrefixRepository prefixRepository;

    @BeforeEach
    @Transactional
    public void cleanDB() {
        prefixRepository.deleteAll();
    }

    @Test
    public void createPrefix() {

        var requestBody = new PrefixDto()
                .setName("11523")
                .setOwner("someone")
                .setStatus(2)
                .setUsedBy("someone else")
                .setDomainId(1)
                .setServiceId(1)
                .setProviderId(1);

        var response = given()
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
        assertEquals(2, response.getStatus());
        assertEquals("Medical & Health Sciences", response.getDomainName());
        assertEquals(1, response.getDomainId());
        assertEquals("B2HANDLE", response.getServiceName());
        assertEquals(1, response.getServiceId());
        assertEquals("GRNET", response.getProviderName());
        assertEquals(1, response.getProviderId());
    }

    @Test
    public void createPrefixWithNameAlreadyExists() {

        var requestBody = new PrefixDto()
                .setName("11527")
                .setOwner("someone")
                .setStatus(2)
                .setUsedBy("someone else")
                .setDomainId(1)
                .setServiceId(1)
                .setProviderId(1);

        // create the prefix
        given().contentType(ContentType.JSON)
                .body(requestBody)
                .post();

        var response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post()
                .then()
                .assertThat()
                .statusCode(409)
                .extract()
                .as(APIError.class);

        assertEquals("Prefix name already exists", response.getMessage());
    }

    @Test
    public void createPrefixWithInvalidService() {

        var requestBody = new PrefixDto()
                .setName("11524")
                .setOwner("someone")
                .setStatus(2)
                .setUsedBy("someone else")
                .setDomainId(1)
                .setServiceId(999)
                .setProviderId(1);

        var response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post()
                .then()
                .assertThat()
                .statusCode(404)
                .extract()
                .as(APIError.class);

        assertEquals("Service not found", response.getMessage());

    }

    @Test
    public void createPrefixWithInvalidDomain() {

        var requestBody = new PrefixDto()
                .setName("11524")
                .setOwner("someone")
                .setStatus(2)
                .setUsedBy("someone else")
                .setDomainId(999)
                .setServiceId(1)
                .setProviderId(1);

        var response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post()
                .then()
                .assertThat()
                .statusCode(404)
                .extract()
                .as(APIError.class);

        assertEquals("Domain not found", response.getMessage());
    }

    @Test
    public void createPrefixWithInvalidProvider() {

        var requestBody = new PrefixDto()
                .setName("11524")
                .setOwner("someone")
                .setStatus(2)
                .setUsedBy("someone else")
                .setDomainId(1)
                .setServiceId(1)
                .setProviderId(999);

        var response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post()
                .then()
                .assertThat()
                .statusCode(404)
                .extract()
                .as(APIError.class);

        assertEquals("Provider not found", response.getMessage());

    }

    @Test
    public void deletePrefix(){

        // creating a new Prefix
        var requestBody = new PrefixDto()
                .setName("11545")
                .setOwner("someone")
                .setStatus(2)
                .setUsedBy("someone else")
                .setDomainId(1)
                .setServiceId(1)
                .setProviderId(1);

        var response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post()
                .then()
                .assertThat()
                .statusCode(201)
                .extract()
                .as(PrefixResponseDto.class);

        // deleting an existing Prefix

        given()
                .delete("/{id}", response.id)
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void deletePrefixNotfound(){

        // creating a new Prefix
        var requestBody = new PrefixDto()
                .setName("11545")
                .setOwner("someone")
                .setStatus(2)
                .setUsedBy("someone else")
                .setDomainId(1)
                .setServiceId(1)
                .setProviderId(1);

        var response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post()
                .then()
                .assertThat()
                .statusCode(201)
                .extract()
                .as(PrefixResponseDto.class);

        // deleting an existing Prefix

       var resp =  given()
                .delete("/{id}", response.id + 10)
                .then()
                .assertThat()
                .statusCode(404)
                .extract()
                .as(APIError.class);

       assertEquals("Prefix not found", resp.getMessage());
    }

    @Test
    public void fetchPrefixById(){

        var requestBody = new PrefixDto()
                .setName("12345")
                .setOwner("someone")
                .setStatus(2)
                .setUsedBy("someone else")
                .setDomainId(1)
                .setServiceId(1)
                .setProviderId(1);

        var created = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post()
                .then()
                .assertThat()
                .statusCode(201)
                .extract()
                .as(PrefixResponseDto.class);

        var prefixResponseDto = given()
                .get("/{id}", created.id)
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(PrefixResponseDto.class);

        assertEquals(created.name, prefixResponseDto.name);
        assertEquals(created.domainId, prefixResponseDto.domainId);
        assertEquals(created.id, prefixResponseDto.id);
    }

    @Test
    public void fetchPrefixByIdNotFound() {
        var resp =  given()
                .get("/{id}",999)
                .then()
                .assertThat()
                .statusCode(404)
                .extract()
                .as(APIError.class);

        assertEquals("Prefix not found", resp.getMessage());
    }

    @Test
    public void testFetchAllPrefixes(){

        var prefixes = prefixRepository.findAll().list();

        var prefixResponseDto  = given()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(PrefixResponseDto[].class);

        assertEquals(prefixes.size(), prefixResponseDto.length);
    }
}
