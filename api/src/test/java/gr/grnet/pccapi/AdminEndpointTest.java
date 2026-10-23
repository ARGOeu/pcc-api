package gr.grnet.pccapi;

import gr.grnet.pccapi.dto.AssignProviderDto;
import gr.grnet.pccapi.dto.ContactDto;
import gr.grnet.pccapi.dto.PrefixRequestInitialDto;
import gr.grnet.pccapi.dto.PrefixRequestResponse;
import gr.grnet.pccapi.dto.provider.ProviderRequestDto;
import gr.grnet.pccapi.dto.provider.ProviderResponseDTO;
import gr.grnet.pccapi.endpoint.AdminEndpoint;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.service.PrefixRequestService;
import gr.grnet.pccapi.service.ProviderService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestHTTPEndpoint(AdminEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class AdminEndpointTest extends KeycloakTest {

    @Inject
    PrefixRequestService prefixRequestService;

    @Inject
    ProviderService providerService;

    @BeforeEach
    void setupAuthorization() {
        mockSuperAdmin();

        prefixRequestService.deleteAll();
    }

    @AfterEach
    void deleteProvider() {
        providerService.deleteByName("TEST_PROVIDER");
    }

    @Test
    public void getRoleMetadata() {

        var response = authenticatedRequest()
                .get("/roles/metadata")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertNotNull(response);
    }

    @Test
    public void getRoleAssignmentMetadata() {

        var response = authenticatedRequest()
                .get("/roles/assignment/metadata")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertNotNull(response.get("resources.Provider"));
        assertNotNull(response.get("resources.Prefix"));
    }

    private RequestSpecification authenticatedRequest() {

        return given()
                .header("Authorization", "Bearer " + adminToken);
    }


    @Test
    void assignProvider_validRequest_returnsOk() {
            var prefixRequest = createPrefixRequest(
                    prefixRequestJson(
                            "johndoe@gmail.com",
                            "PROJECT",
                            "https://prefix-create-test.example.com"));


            var provider = createProvider("TEST_PROVIDER");

            var request = new AssignProviderDto();
            request.setProviderId(provider.getId());

            var response = authenticatedRequest()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .patch("/prefix-requests/{id}/assign-provider", prefixRequest.id)
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(PrefixRequestResponse.class);

            assertEquals(provider.getId(), response.providerId);
            assertEquals("TEST_PROVIDER", response.providerName);

    }


    private PrefixRequestResponse createPrefixRequest(PrefixRequestInitialDto requestBody) {
        return prefixRequestService.create(requestBody);
    }

    private PrefixRequestInitialDto prefixRequestJson(
            String email,
            String projectName,
            String projectUrl) {
        var req = new PrefixRequestInitialDto();
        req.setContactPersonEmail(email);
        req.setProjectName(projectName);
        req.setProjectUrl(projectUrl);
        req.setContactPersonName("Test");
        req.setContactPersonSurname("User");
        req.setContactPersonPhone("2103911103");
        req.setCountry("Greece");
        req.setOrganizationName("GRNET");
        req.setProjectDescription("Test project description");
        req.setExpectedHandles("UP_TO_1000");
        return req;
    }

    private ProviderResponseDTO createProvider(String provider) {
        var req = new ProviderRequestDto();
        req.name = provider;
        req.description = "this is a description";
        req.contacts = new ArrayList<ContactDto>();
        return providerService.create(req);
    }

    @Test
    void assignProvider_prefixRequestNotFound_returns404() {
        var provider = createProvider("TEST_PROVIDER");

        var request = new AssignProviderDto();
        request.setProviderId(provider.getId());

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .patch("/prefix-requests/{id}/assign-provider", 999999)
                .then()
                .statusCode(404);
    }

    @Test
    void assignProvider_providerNotFound_returns404() {
        var prefixRequest = createPrefixRequest(
                prefixRequestJson(
                        "johndoe@gmail.com",
                        "PROJECT",
                        "https://prefix-create-test.example.com"));

        var request = new AssignProviderDto();
        request.setProviderId(999999);

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(request)
                .patch("/prefix-requests/{id}/assign-provider", prefixRequest.id)
                .then()
                .statusCode(404);
    }

    @Test
    void assignProvider_missingBody_returns400() {
        var prefixRequest = createPrefixRequest(
                prefixRequestJson(
                        "johndoe@gmail.com",
                        "PROJECT",
                        "https://prefix-create-test.example.com"));

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .patch("/prefix-requests/{id}/assign-provider", prefixRequest.id)
                .then()
                .statusCode(400);
    }

    @Test
    void assignProvider_missingProviderId_returns400() {
        var prefixRequest = createPrefixRequest(
                prefixRequestJson(
                        "johndoe@gmail.com",
                        "PROJECT",
                        "https://prefix-create-test.example.com"));

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body("{}")
                .patch("/prefix-requests/{id}/assign-provider", prefixRequest.id)
                .then()
                .statusCode(400);
    }

    @Test
    void assignProvider_reassignProvider_returnsOk() {
        var prefixRequest = createPrefixRequest(
                prefixRequestJson(
                        "johndoe@gmail.com",
                        "PROJECT",
                        "https://prefix-create-test.example.com"));

        var firstProvider = createProvider("FIRST_PROVIDER");
        var secondProvider = createProvider("SECOND_PROVIDER");

        var firstRequest = new AssignProviderDto();
        firstRequest.setProviderId(firstProvider.getId());

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(firstRequest)
                .patch("/prefix-requests/{id}/assign-provider", prefixRequest.id)
                .then()
                .statusCode(200);

        var secondRequest = new AssignProviderDto();
        secondRequest.setProviderId(secondProvider.getId());

        var response = authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(secondRequest)
                .patch("/prefix-requests/{id}/assign-provider", prefixRequest.id)
                .then()
                .statusCode(200)
                .extract()
                .as(PrefixRequestResponse.class);

        assertEquals(secondProvider.getId(), response.providerId);
        assertEquals("SECOND_PROVIDER", response.providerName);
        providerService.deleteByName("SECOND_PROVIDER");
        providerService.deleteByName("FIRST_PROVIDER");

    }
}
