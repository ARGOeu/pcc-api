package gr.grnet.pccapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.FiltersDto;
import gr.grnet.pccapi.dto.HandleDto;
import gr.grnet.pccapi.endpoint.ReverseLookUpEndpoint;
import gr.grnet.pccapi.wiremock.HRLSWiremockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@QuarkusTestResource(HRLSWiremockServer.class)
@TestProfile(PCCApiTestProfile.class)
@TestHTTPEndpoint(ReverseLookUpEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReverseLookupEndpointTest {

  @Test
  public void reverseLookUpSuccessFull() {

    var filters = new FiltersDto();
    filters.setFilters(
        Map.of(
            "URL", "domain.com",
            "EMAIL", "me@mail.com",
            "RETRIEVE_RECORDS", "true"));

    var response =
        given()
            .queryParams(
                Map.of(
                    "page", "1",
                    "limit", "20"))
            .contentType(ContentType.JSON)
            .body(filters)
            .post()
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(HandleDto[].class);

    var h1 = new HandleDto().setHandle("11098/EUDAT-JMD_001B3286-527B-5225-B68D-FBF2C775D49D");
    h1.addType("URL", "http://b2find.eudat.eu/dataset/001b3286-527b-5225-b68d-fbf2c775d49d");
    h1.addType("CHECKSUM", "f7b24993990ba4fc2104b09b63e7f975");
    h1.addType(
        "EUDAT/ROR", "http://eudatmd1.dkrz.de:8080/dataset/001b3286-527b-5225-b68d-fbf2c775d49d");

    var h2 = new HandleDto().setHandle("11098/EUDAT-JMD_00504D2A-EF64-51EB-AC5D-8A91485E35B4");
    h2.addType("URL", "http://b2find.eudat.eu/dataset/00504d2a-ef64-51eb-ac5d-8a91485e35b4");
    h2.addType("CHECKSUM", "ba23d43ae049c86db3e8cc284683e08c");
    h2.addType("EUDAT/METADATATYPE", "http://www.openarchives.org/OAI/2.0/oai_dc.xsd");

    assertEquals(h1, response[0]);
    assertEquals(h2, response[1]);
  }

  @Test
  public void reverseLookUpSuccessFullChecksum() {

    var filters = new FiltersDto();
    filters.setFilters(
        Map.of(
            "CHECKSUM", "f7b24993990ba4fc2104b09b63e7f975",
            "URL", "domain.com",
            "EMAIL", "me@mail.com",
            "RETRIEVE_RECORDS", "true"));

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(filters)
            .post()
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(HandleDto[].class);

    var h1 = new HandleDto().setHandle("11098/EUDAT-JMD_001B3286-527B-5225-B68D-FBF2C775D49D");
    h1.addType("URL", "http://b2find.eudat.eu/dataset/001b3286-527b-5225-b68d-fbf2c775d49d");
    h1.addType("CHECKSUM", "f7b24993990ba4fc2104b09b63e7f975");
    h1.addType(
        "EUDAT/ROR", "http://eudatmd1.dkrz.de:8080/dataset/001b3286-527b-5225-b68d-fbf2c775d49d");

    assertEquals(h1, response[0]);
  }

  @Test
  public void reverseLookUpSuccessFullEudatChecksum() {

    var filters = new FiltersDto();
    filters.setFilters(
        Map.of(
            "EUDAT_CHECKSUM", "sha2:3UGI6sWL/dGhINcuH2AaxravBuiMq30ZAcc1rs1yNZI=",
            "URL", "domain.com",
            "EMAIL", "me@mail.com",
            "RETRIEVE_RECORDS", "true"));

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(filters)
            .post()
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(HandleDto[].class);

    var h1 = new HandleDto().setHandle("11098/EUDAT-JMD_001B3286-527B-5225-B68D-FBF2C775D49D");
    h1.addType("URL", "http://b2find.eudat.eu/dataset/001b3286-527b-5225-b68d-fbf2c775d49d");
    h1.addType("EUDAT/CHECKSUM", "sha2:3UGI6sWL/dGhINcuH2AaxravBuiMq30ZAcc1rs1yNZI=");
    h1.addType(
        "EUDAT/ROR", "http://eudatmd1.dkrz.de:8080/dataset/001b3286-527b-5225-b68d-fbf2c775d49d");

    assertEquals(h1, response[0]);
  }

  @Test
  public void reverseLookUpSuccessFullLoc() {

    var filters = new FiltersDto();
    filters.setFilters(
        Map.of(
            "LOC",
                "<locations><location href=\"http://repo.data.ingv.it/INGV/home/rods/san/archive/2016/IV/MABI/HHE.D\" id=\"0\"/></locations>",
            "URL", "domain.com",
            "EMAIL", "me@mail.com",
            "RETRIEVE_RECORDS", "true"));

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(filters)
            .post()
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(HandleDto[].class);

    var h1 = new HandleDto().setHandle("11098/EUDAT-JMD_001B3286-527B-5225-B68D-FBF2C775D49D");
    h1.addType("URL", "http://b2find.eudat.eu/dataset/001b3286-527b-5225-b68d-fbf2c775d49d");
    h1.addType(
        "10320/LOC",
        "<locations><location href=\\\"http://repo.data.ingv.it/INGV/home/rods/san/archive/2016/IV/MABI/HHE.D\\\" id=\\\"0\\\"/></locations>\"");
    h1.addType(
        "EUDAT/ROR", "http://eudatmd1.dkrz.de:8080/dataset/001b3286-527b-5225-b68d-fbf2c775d49d");

    assertEquals(h1, response[0]);
  }

  @Test
  public void reverseLookUpSuccessFlat() {

    var filters = new FiltersDto();
    filters.setFilters(
        Map.of(
            "URL", "domain.com",
            "EMAIL", "me@mail.com",
            "RETRIEVE_RECORDS", "false"));

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(filters)
            .post()
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(HandleDto[].class);

    var h1 = new HandleDto().setHandle("11098/EUDAT-JMD_001B3286-527B-5225-B68D-FBF2C775D49D");
    var h2 = new HandleDto().setHandle("11098/EUDAT-JMD_00504D2A-EF64-51EB-AC5D-8A91485E35B4");

    assertEquals(h1, response[0]);
    assertEquals(h2, response[1]);
  }

  @Test
  public void reverseLookUpWrongFilter() {

    var filters = new FiltersDto();
    filters.setFilters(
        Map.of(
            "URL", "domain.com",
            "EMAIL", "me@mail.com",
            "RETRIEVE_RECORDS", "false",
            "Invalid", "invalid"));

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(filters)
            .post()
            .then()
            .assertThat()
            .statusCode(400)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Filter Invalid is unsupported", response.getMessage());
  }

  @Test
  public void reverseLookUpClientError() {

    var filters = new FiltersDto();
    filters.setFilters(
        Map.of(
            "URL", "invalid",
            "RETRIEVE_RECORDS", "false"));

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(filters)
            .post()
            .then()
            .assertThat()
            .statusCode(500)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("INTERNAL_SERVER_ERROR", response.getMessage());
  }

  @Test
  public void testFiltersSuccess() {
    var response =
        given()
            .contentType(ContentType.JSON)
            .get("/filters")
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(String[].class);

    assertEquals(6, response.length);
    assertEquals("URL", response[0]);
    assertEquals("EMAIL", response[1]);
    assertEquals("CHECKSUM", response[2]);
    assertEquals("EUDAT_CHECKSUM", response[3]);
    assertEquals("LOC", response[4]);
    assertEquals("RETRIEVE_RECORDS", response[5]);
  }

  @Test
  public void testTypesSuccess() {
    var response =
        given()
            .contentType(ContentType.JSON)
            .get("/types")
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .as(String[].class);

    assertEquals(4, response.length);
    assertEquals("CENTRAL", response[0]);
    assertEquals("PRIVATE", response[1]);
    assertEquals("BOTH", response[2]);
    assertEquals("NONE", response[3]);
  }
}
