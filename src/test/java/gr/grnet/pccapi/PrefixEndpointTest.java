package gr.grnet.pccapi;

import gr.grnet.pccapi.dto.DomainDto;
import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.dto.PrefixResponseDto;
import gr.grnet.pccapi.endpoint.PrefixEndpoint;
import gr.grnet.pccapi.entity.Prefix;
import gr.grnet.pccapi.repository.PrefixRepository;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;


import javax.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(PrefixEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PrefixEndpointTest {

    @Inject
    PrefixRepository prefixRepository;

    @Test
    public void testCreatePrefix() {

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

    }

    //todo change with proper exception
    @Test
    public void testCreatePrefixWithNameAlreadyExists() {

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
                .statusCode(500);
    }

    //todo change with proper exception
    @Test
    public void testCreatePrefixWithInvalidService() {

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
                .statusCode(500);
    }

    //todo change with proper exception
    @Test
    public void testCreatePrefixWithInvalidDomain() {

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
                .statusCode(500);
    }

    @Test
    public void testCreatePrefixWithInvalidProvider() {

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
                .statusCode(500);
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
}
