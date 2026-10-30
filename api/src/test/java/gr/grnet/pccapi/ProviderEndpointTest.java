
package gr.grnet.pccapi;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.ContactDto;
import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.dto.provider.ProviderResponseDTO;
import gr.grnet.pccapi.endpoint.ProviderEndpoint;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(ProviderEndpoint.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestProfile(PCCApiTestProfile.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class ProviderEndpointTest extends KeycloakTest {

    private final List<Integer> createdProviderIds = new ArrayList<>();

    @ConfigProperty(name = "base.upload.logo.dir")
    String uploadDir;

    @BeforeEach
    void setupAuthorization() {
        mockSuperAdmin();
    }

    @AfterEach
    void cleanup() throws IOException {

        for (Integer id : createdProviderIds) {
            try {
                deleteProvider(id);
            } finally {
                Files.deleteIfExists(Path.of(uploadDir, id + ".png"));
                Files.deleteIfExists(Path.of(uploadDir, id + ".jpg"));
            }
        }

        createdProviderIds.clear();
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

        assertEquals(6, response.length);

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

    @Test
    void createProviderWithContact() {

        String providerName = "PROVIDER-CREATE-TEST";
        String contactName = "Create Test Contact";
        String email = "create-test@example.com";

        String requestBody = providerJson(
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
        String contact = contactJson(contactName, email, "ADMIN");

        String firstProviderName = "PROVIDER-SHARED-1";
        String secondProviderName = "PROVIDER-SHARED-2";

        ProviderResponseDTO firstResponse =
                createProvider(providerJson(firstProviderName, contact));

        ProviderResponseDTO secondResponse =
                createProvider(providerJson(secondProviderName, contact));

        assertEquals(1, firstResponse.getContacts().size());
        assertEquals(1, secondResponse.getContacts().size());

        ContactDto firstContact =
                firstResponse.getContacts().iterator().next();

        ContactDto secondContact =
                secondResponse.getContacts().iterator().next();

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

        ProviderResponseDTO updateResponse =
                updateProvider(providerId, updateBody);

        assertNotNull(updateResponse);
        assertEquals(providerName, updateResponse.getName());
        assertEquals(1, updateResponse.getContacts().size());

        ContactDto updatedContact =
                updateResponse.getContacts().iterator().next();

        assertEquals("New Contact", updatedContact.name);
        assertEquals("new-contact@example.com", updatedContact.email);
    }

    @Test
    void deleteProviderWithoutPrefixes() {

        String providerName = "PROVIDER-DELETE-TEST";

        String contact =
                contactJson("Delete Test Contact",
                        "delete-test@example.com", "ADMIN");

        ProviderResponseDTO createResponse =
                createProvider(providerJson(providerName, contact));

        Integer providerId = createResponse.id;

        InformativeResponse deleteResponse = deleteProvider(providerId);

        createdProviderIds.remove(providerId);

        assertEquals(200, deleteResponse.code);
    }

    // ------------------------------------------------
    // Provider Logo Tests
    // ------------------------------------------------

    @Test
    void createProviderWithLogoUrl() {

        String logo = "https://example.com/logo.png";

        String requestBody = providerJson(
                uniqueProviderName(),
                logo,
                testContact());

        ProviderResponseDTO response = createProvider(requestBody);

        assertNotNull(response.id);
        assertEquals(logo, response.getLogo());

        ProviderResponseDTO fetched = fetchProvider(response.id);
        assertEquals(logo, fetched.getLogo());
    }

    @Test
    void createProviderWithBase64Logo() throws IOException {

        String requestBody = providerJson(
                uniqueProviderName(),
                base64Logo(),
                testContact());

        ProviderResponseDTO response = createProvider(requestBody);

        assertNotNull(response.id);
        assertNotNull(response.getLogo());
        assertTrue(response.getLogo()
                .endsWith("/logos/" + response.id + ".png"));

        assertFalse(response.getLogo().startsWith("data:image/"));

        Path imagePath = logoPath(response.id, ".png");

        assertTrue(Files.exists(imagePath));
        assertTrue(Files.size(imagePath) > 0);

        ProviderResponseDTO fetched = fetchProvider(response.id);

        assertEquals(response.getLogo(), fetched.getLogo());
    }

    @Test
    void updateProviderWithLogoUrl() {

        String oldLogo = "https://example.com/old.png";
        String newLogo = "https://example.com/new.jpg";

        ProviderResponseDTO created = createProvider(
                providerJson(uniqueProviderName(), oldLogo, testContact()));

        ProviderResponseDTO updated = updateProvider(
                created.id,
                updateProviderJson(newLogo));

        assertEquals(newLogo, updated.getLogo());
        assertEquals(newLogo, fetchProvider(created.id).getLogo());
    }

    @Test
    void updateProviderWithBase64Logo() throws IOException {

        ProviderResponseDTO created = createProvider(
                providerJson(
                        uniqueProviderName(),
                        "https://example.com/old.png",
                        testContact()));

        ProviderResponseDTO updated = updateProvider(
                created.id,
                updateProviderJson(base64Logo()));

        assertNotNull(updated.getLogo());
        assertTrue(updated.getLogo()
                .endsWith("/logos/" + created.id + ".png"));

        assertTrue(Files.exists(logoPath(created.id, ".png")));

        assertEquals(updated.getLogo(),
                fetchProvider(created.id).getLogo());
    }

    @Test
    void updateProviderWithoutChangingLogo() {

        String originalLogo = "https://example.com/original.png";

        ProviderResponseDTO created = createProvider(
                providerJson(
                        uniqueProviderName(),
                        originalLogo,
                        testContact()));

        String requestBody = """
                {
                  "description": "Updated provider description",
                  "contacts": [%s]
                }
                """.formatted(testContact());

        ProviderResponseDTO updated =
                updateProvider(created.id, requestBody);

        assertEquals(originalLogo, updated.getLogo());
        assertEquals(originalLogo,
                fetchProvider(created.id).getLogo());
    }

    @Test
    void updateProviderFromBase64ToExternalUrl() throws IOException {

        ProviderResponseDTO created = createProvider(
                providerJson(
                        uniqueProviderName(),
                        base64Logo(),
                        testContact()));

        Path imagePath = logoPath(created.id, ".png");

        assertTrue(Files.exists(imagePath));

        String newLogo = "https://example.com/replacement.png";

        ProviderResponseDTO updated = updateProvider(
                created.id,
                updateProviderJson(newLogo));

        assertEquals(newLogo, updated.getLogo());
        assertEquals(newLogo,
                fetchProvider(created.id).getLogo());

        assertFalse(Files.exists(imagePath),
                "Previous uploaded logo should be deleted");
    }

    @Test
    void createProviderWithInvalidLogo() {

        String requestBody = providerJson(
                uniqueProviderName(),
                "invalid-logo",
                testContact());

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    void updateProviderWithInvalidLogo() {

        String originalLogo = "https://example.com/original.png";

        ProviderResponseDTO created = createProvider(
                providerJson(
                        uniqueProviderName(),
                        originalLogo,
                        testContact()));

        authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(updateProviderJson("invalid-logo"))
                .when()
                .patch("/{id}", created.id)
                .then()
                .statusCode(400);

        assertEquals(originalLogo,
                fetchProvider(created.id).getLogo());
    }

    // ------------------------------------------------
    // Helpers
    // ------------------------------------------------

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

    private String providerJson(
            String name,
            String logo,
            String contactsJson) {

        return """
                {
                  "name": "%s",
                  "description": "Provider logo test",
                  "logo": "%s",
                  "contacts": [%s]
                }
                """.formatted(name, logo, contactsJson);
    }

    private String updateProviderJson(String logo) {

        return """
                {
                  "description": "Updated provider description",
                  "logo": "%s",
                  "contacts": [%s]
                }
                """.formatted(logo, testContact());
    }

    private String testContact() {

        return contactJson(
                "Logo Test Contact",
                "logo-test@example.com",
                "ADMIN");
    }

    private String uniqueProviderName() {
        return "PROVIDER-LOGO-" + UUID.randomUUID();
    }

    private ProviderResponseDTO createProvider(String requestBody) {

        ProviderResponseDTO response = authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .as(ProviderResponseDTO.class);

        // Track only providers created by logo tests.
        if (response.name != null &&
                response.name.startsWith("PROVIDER-LOGO-")) {
            createdProviderIds.add(response.id);
        }

        return response;
    }

    private ProviderResponseDTO updateProvider(
            Integer id,
            String requestBody) {

        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/{id}", id)
                .then()
                .statusCode(200)
                .extract()
                .as(ProviderResponseDTO.class);
    }

    private ProviderResponseDTO fetchProvider(Integer id) {

        return authenticatedRequest()
                .contentType(ContentType.JSON)
                .when()
                .get("/{id}", id)
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
                .statusCode(200)
                .extract()
                .as(InformativeResponse.class);
    }

    private Path logoPath(Integer providerId, String extension) {
        return Path.of(uploadDir, providerId + extension);
    }

    private String base64Logo() throws IOException {

        var image = new BufferedImage(
                10,
                10,
                BufferedImage.TYPE_INT_RGB);

        var output = new ByteArrayOutputStream();

        assertTrue(ImageIO.write(image, "png", output));

        return "data:image/png;base64," +
                Base64.getEncoder()
                        .encodeToString(output.toByteArray());
    }

    private RequestSpecification authenticatedRequest() {
        return given()
                .header("Authorization", "Bearer " + adminToken);
    }
}
