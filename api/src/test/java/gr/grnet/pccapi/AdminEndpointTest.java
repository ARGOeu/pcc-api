package gr.grnet.pccapi;

import gr.grnet.pccapi.dto.PrefixRequestInitialDto;
import gr.grnet.pccapi.dto.PrefixRequestResponse;
import gr.grnet.pccapi.dto.PrefixRequestStatusDto;
import gr.grnet.pccapi.endpoint.AdminEndpoint;
import gr.grnet.pccapi.enums.PrefixRequestStatus;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestHTTPEndpoint(AdminEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class AdminEndpointTest extends KeycloakTest {

    @BeforeEach
    void setupAuthorization() {
        mockSuperAdmin();
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
    void updateStatus_pendingToAccepted_returnsOk() {
        var response = createPrefixRequest(
                prefixRequestJson(
                        "johndoe@gmail.com",
                        "PROJECT",
                        "https://prefix-create-test.example.com"));

        var statusReq = new PrefixRequestStatusDto();
        statusReq.status = PrefixRequestStatus.ACCEPTED.name();
        statusReq.reason="this is a reason";

        var updatedResponse = authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(statusReq)
                .patch("/prefix-requests/{id}/status", response.id)
                .then()
                .statusCode(200)
                .extract()
                .as(PrefixRequestResponse.class);

        assertEquals(
                PrefixRequestStatus.ACCEPTED,
                updatedResponse.status);
    }
    @Test
    void updateStatus_pendingToRejected_returnsOk() {
        var response= createPrefixRequest(    prefixRequestJson(
                "johndoe@gmail.com",
                "PROJECT",
                "https://prefix-create-test.example.com"));
        var statusReq=new PrefixRequestStatusDto();
        statusReq.status=PrefixRequestStatus.REJECTED.name();
        statusReq.reason="this is a reason";

        var updatedResponse = authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(statusReq)
                .patch("/prefix-requests/{id}/status", response.id)
                .then()
                .statusCode(200)
                .extract()
                .as(PrefixRequestResponse.class);

        assertEquals(
                PrefixRequestStatus.REJECTED,
                updatedResponse.status);

    }


    @Test
    void updateStatus_requestNotFound_returnsNotFound() {
        var statusReq=new PrefixRequestStatusDto();
        statusReq.status=PrefixRequestStatus.ACCEPTED.name();
        statusReq.reason="this is a reason";

        var updatedResponse = authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(statusReq)
                .patch("/prefix-requests/{id}/status", 99999)
                .then()
                .statusCode(404);

    }
//
    @Test
    void updateStatus_pendingToPending_returnsBadRequest() {
        var response= createPrefixRequest(    prefixRequestJson(
                "johndoe@gmail.com",
                "PROJECT",
                "https://prefix-create-test.example.com"));
        var statusReq=new PrefixRequestStatusDto();
        statusReq.status=PrefixRequestStatus.PENDING.name();
        statusReq.reason="this is a reason";
        var updatedResponse = authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(statusReq)
                .patch("/prefix-requests/{id}/status", response.id)
                .then()
                .statusCode(400);

    }
//
//    @Test
    void updateStatus_invalidStatus_returnsBadRequest() {
        var response= createPrefixRequest(    prefixRequestJson(
                "johndoe@gmail.com",
                "PROJECT",
                "https://prefix-create-test.example.com"));
        var statusReq=new PrefixRequestStatusDto();
        statusReq.status="WHATEVER";
        statusReq.reason="this is a reason";
        var updatedResponse = authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(statusReq)
                .patch("/prefix-requests/{id}/status", response.id)
                .then()
                .statusCode(400);

    }
//
    @Test
    void updateStatus_missingStatus_returnsBadRequest() {
        var response= createPrefixRequest(    prefixRequestJson(
                "johndoe@gmail.com",
                "PROJECT",
                "https://prefix-create-test.example.com"));
       // var statusReq=new PrefixRequestStatusDto();

        var updatedResponse = authenticatedRequest()
                .contentType(ContentType.JSON)
                .patch("/prefix-requests/{id}/status", response.id)
                .then()
                .statusCode(400);
    }

    private PrefixRequestResponse createPrefixRequest(PrefixRequestInitialDto requestBody) {
      return  prefixRequestService.create(requestBody);
    }

    private PrefixRequestInitialDto prefixRequestJson(
            String email,
            String projectName,
            String projectUrl) {
        var req=new PrefixRequestInitialDto();
        req.setContactPersonEmail(email);
        req.setProjectName(projectName);
        req.setProjectUrl(projectUrl);
        req.setContactPersonName("Test");
        req.setContactPersonSurname( "User");
        req.setContactPersonPhone("2103911103");
        req.setCountry( "Greece");
        req.setOrganizationName( "GRNET");
        req.setProjectDescription( "Test project description");
        req.setExpectedHandles( "UP_TO_1000");
        return req;
    }

}