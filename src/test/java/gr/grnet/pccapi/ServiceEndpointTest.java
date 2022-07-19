package gr.grnet.pccapi;

import gr.grnet.pccapi.dto.ServiceDto;
import gr.grnet.pccapi.endpoint.ServiceEndpoint;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(ServiceEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServiceEndpointTest {

    @Test
    public void listAllServices() {

        var response = given()
                .contentType(ContentType.JSON)
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(ServiceDto[].class);

        assertEquals(3, response.length);
        assertEquals(1L, response[0].id);
        assertEquals("B2HANDLE", response[0].name);

        assertEquals(2L, response[1].id);
        assertEquals("B2SAFE", response[1].name);

        assertEquals(3L, response[2].id);
        assertEquals("B2ACCESS", response[2].name);
    }

}
