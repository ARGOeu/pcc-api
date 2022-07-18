package gr.grnet.pccapi;

import gr.grnet.pccapi.client.EOSCPortalClient;
import gr.grnet.pccapi.dto.DomainDto;
import gr.grnet.pccapi.endpoint.DomainEndpoint;
import gr.grnet.pccapi.repository.DomainRepository;
import gr.grnet.pccapi.service.DomainService;
import gr.grnet.pccapi.wiremock.EOSCPortalDomainWiremockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(EOSCPortalDomainWiremockServer.class)
@TestProfile(PCCApiTestProfile.class)
@TestHTTPEndpoint(DomainEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DomainEndpointTest {

    @Inject
    @RestClient
    EOSCPortalClient eoscPortalClient;

    @Inject
    DomainService domainService;

    @Inject
    DomainRepository domainRepository;


    @BeforeAll
    public void setup() throws ExecutionException, InterruptedException {

        var eoscPortalDomains = eoscPortalClient.getByType("SCIENTIFIC_DOMAIN").toCompletableFuture().get();

        domainService.saveEoscPortalDomains(eoscPortalDomains);
    }

    @Test
    public void fetchDomainById(){

       var domain = domainRepository.findById(1);

       var domainDto = given()
                .get("/{id}", 1)
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(DomainDto.class);

       assertEquals(domainDto.name, domain.name);
       assertEquals(domainDto.domainId, domain.domainId);
       assertEquals(domainDto.description, domain.description);
    }
}
