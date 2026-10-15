package gr.grnet.pccapi;

import static io.restassured.RestAssured.given;
import static io.smallrye.common.constraint.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.ContactDto;
import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.dto.prefix.PrefixResponseDto;
import gr.grnet.pccapi.dto.provider.ProviderResponseDTO;
import gr.grnet.pccapi.endpoint.ProviderEndpoint;
import gr.grnet.pccapi.entity.Contact;
import gr.grnet.pccapi.entity.Provider;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;

@QuarkusTest
@TestHTTPEndpoint(ProviderEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class ProviderEndpointTest extends KeycloakTest {

  @BeforeEach
  void setupAuthorization() {
    mockSuperAdmin();
  }

  @Test
  public void getById() {

    var response = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .get("/{id}", 1)
            .then()
            .statusCode(200)
            .extract()
            .as(ProviderResponseDTO.class);

    assertEquals(1, response.id);
    assertEquals("GRNET", response.name);
  }

  @Test
  public void getList() {

    var response = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .get()
            .then()
            .statusCode(200)
            .extract()
            .as(ProviderResponseDTO[].class);

    assertEquals(3, response.length);

    assertTrue(Arrays.stream(response)
            .anyMatch(provider ->
                    provider.id == 1
                            && "GRNET".equals(provider.name)));

    assertTrue(Arrays.stream(response)
            .anyMatch(provider ->
                    provider.id == 3
                            && "Surf".equals(provider.name)));

    assertTrue(Arrays.stream(response)
            .anyMatch(provider ->
                    provider.id == 4
                            && "GWDG".equals(provider.name)));
  }

  @Test
  public void getByIdNotfound() {

    var response = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .get("/{id}", 999)
            .then()
            .statusCode(404)
            .extract()
            .as(APIResponseMsg.class);

    assertEquals("Provider not found", response.getMessage());
  }

  private String contactJson(String name, String email, String type) {
    return """
        {
          "name": "%s",
          "email": "%s",
          "type": "%s"
        }
        """.formatted(name, email, type);
  }

  private String providerJson(String name, String contactsJson) {
    return """
        {
          "name": "%s",
          "contacts": [%s]
        }
        """.formatted(name, contactsJson);
  }

  private ProviderResponseDTO createProvider(String requestBody) {
    return authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post()
            .then()
            .statusCode(201)
            .extract()
            .as(ProviderResponseDTO.class);

  }

  private ProviderResponseDTO updateProvider(Integer id, String requestBody) {
    return authenticatedRequest()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .put("/{id}", id)
            .then()
            .statusCode(200)
            .extract()
            .as(ProviderResponseDTO.class);

  }

  private InformativeResponse deleteProvider(Integer id) {
    return authenticatedRequest()
            .when()
            .delete("/{id}", id)
            .then()
            .statusCode(204)
            .extract()
            .as(InformativeResponse.class);

  }

  @Test
  void createProviderWithContact() {
    String providerName = "PROVIDER-CREATE-TEST";
    String contactName = "Create Test Contact";
    String email = "create-test@example.com";

    String requestBody =
            providerJson(
                    providerName,
                    contactJson(contactName, email, "ADMIN"));

    ProviderResponseDTO response = createProvider(requestBody);

    assertEquals(providerName, response.getName());
    assertNotNull(response.getContacts());
    assertEquals(1, response.getContacts().size());

    ContactDto contact = response.getContacts().iterator().next();
    assertEquals(contactName, contact.name);
    assertEquals(email, contact.email);
  }

  @Test
  void createProviderReusesExistingContact() {
    String contactName = "Shared Test Contact";
    String email = "shared-test@example.com";
    String contactJson = contactJson(contactName, email, "ADMIN");

    String firstProviderName = "PROVIDER-SHARED-1";
    String secondProviderName = "PROVIDER-SHARED-2";

    ProviderResponseDTO firstResponse =
            createProvider(providerJson(firstProviderName, contactJson));

    ProviderResponseDTO secondResponse =
            createProvider(providerJson(secondProviderName, contactJson));


    assertEquals(1, firstResponse.getContacts().size());
    assertEquals(1, secondResponse.getContacts().size());

    ContactDto firstContact = firstResponse.getContacts().iterator().next();
    ContactDto secondContact = secondResponse.getContacts().iterator().next();

    // Both providers should reference the same persisted contact.
    assertEquals(firstContact.id, secondContact.id);
  }

  @Test
  void updateProviderReplacesContactsAndKeepsProviderName() {
    String providerName = "PROVIDER-UPDATE-TEST";

    String initialContact =
            contactJson("Old Contact", "old-contact@example.com", "ADMIN");

    ProviderResponseDTO createResponse =
            createProvider(providerJson(providerName, initialContact));


    Integer providerId = createResponse.id;

    String replacementContact =
            contactJson("New Contact", "new-contact@example.com", "ADMIN");

    String updateBody =
            providerJson("THIS-NAME-SHOULD-NOT-CHANGE", replacementContact);

    ProviderResponseDTO updateResponse = updateProvider(providerId, updateBody);

    assertNotNull(updateResponse);
    assertEquals(providerName, updateResponse.getName());
    assertEquals(1, updateResponse.getContacts().size());

    ContactDto updatedContact = updateResponse.getContacts().iterator().next();

    assertEquals("New Contact", updatedContact.name);
    assertEquals("new-contact@example.com", updatedContact.email);
  }

  @Test
  void deleteProviderWithoutPrefixes() {
    String providerName = "PROVIDER-DELETE-TEST";

    String contact =
            contactJson("Delete Test Contact", "delete-test@example.com", "ADMIN");

    ProviderResponseDTO createResponse =
            createProvider(providerJson(providerName, contact));
    Integer providerId = createResponse.id;

    InformativeResponse deleteResponse = deleteProvider(providerId);

    // Change this if your endpoint returns 200 instead of 204.

    assertEquals(204,deleteResponse.code);
  }

  private RequestSpecification authenticatedRequest() {

    return given()
            .header("Authorization", "Bearer " + providerAdminToken)
            .pathParam("id", 1);
  }
}