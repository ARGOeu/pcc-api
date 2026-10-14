
package gr.grnet.pccapi;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import gr.grnet.pccapi.service.HandleCacheService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HandleCacheServiceTest {

    private static final String PREFIX = "21.T15999";
    private static final String OTHER_PREFIX = "21.T99999";
    private static final String AUTHORIZATION = "Basic test";
    private static final String AUTH_FINGERPRINT = "test-fingerprint";

    @Inject
    HandleCacheService cacheService;

    private HttpServer server;
    private String serviceUrl;

    private final AtomicInteger listRequests = new AtomicInteger();
    private final AtomicInteger detailRequests = new AtomicInteger();
    private volatile String metadataValue = "original";
    private volatile boolean includeNewHandle = false;

    @BeforeAll
    void startServer() throws IOException {
        server = HttpServer.create(
                new InetSocketAddress("localhost", 0), 0);

        server.createContext("/api/handles", this::handleRequest);
        server.start();

        serviceUrl = "http://localhost:" + server.getAddress().getPort() + "/";
    }

    @AfterAll
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleRequest(HttpExchange exchange)
            throws IOException {

        var path = exchange.getRequestURI().getPath();

        if (path.equals("/api/handles")) {
            listRequests.incrementAndGet();

            var prefix = exchange.getRequestURI()
                    .getRawQuery()
                    .contains("21.T99999")
                    ? OTHER_PREFIX : PREFIX;

            var secondHandle = includeNewHandle
                    ? ",\"" + prefix + "/NEW\""
                    : "";

            sendJson(exchange, """
                    {
                      "responseCode": 1,
                      "prefix": "%s",
                      "totalCount": "2",
                      "handles": [
                        "%s/A",
                        "%s/B"%s
                      ]
                    }
                    """.formatted(
                    prefix, prefix, prefix, secondHandle));

            return;
        }

        detailRequests.incrementAndGet();

        var identifier = path.substring("/api/handles/".length());

        sendJson(exchange, """
                {
                  "responseCode": 1,
                  "handle": "%s",
                  "values": [
                    {
                      "index": 1,
                      "type": "URL",
                      "data": {
                        "format": "string",
                        "value": "%s"
                      },
                      "ttl": 86400
                    }
                  ]
                }
                """.formatted(identifier, metadataValue));
    }

    private void sendJson(
            HttpExchange exchange, String json)
            throws IOException {

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json");

        exchange.sendResponseHeaders(200, bytes.length);

        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private String uniqueKey(String suffix) {
        return AUTH_FINGERPRINT + "-" + suffix
                + "-" + java.util.UUID.randomUUID();
    }

    @Test
    void shouldCacheIdentifiers() {

        String fingerprint = uniqueKey("identifiers");

        int before = listRequests.get();

        var first = cacheService.getIdentifiers(
                serviceUrl, PREFIX, fingerprint, AUTHORIZATION);

        var second = cacheService.getIdentifiers(
                serviceUrl, PREFIX, fingerprint, AUTHORIZATION);

        assertEquals(first, second);
        assertEquals(2, first.size());

        assertEquals(1, listRequests.get() - before);
    }

    @Test
    void shouldInvalidateOnlySpecifiedPrefix() {

        String fingerprint = uniqueKey("prefix");

        cacheService.getIdentifiers(
                serviceUrl, PREFIX, fingerprint, AUTHORIZATION);

        cacheService.getIdentifiers(
                serviceUrl, OTHER_PREFIX, fingerprint, AUTHORIZATION);

        int before = listRequests.get();

        cacheService.invalidateIdentifiers(
                serviceUrl, PREFIX, fingerprint);

        cacheService.getIdentifiers(
                serviceUrl, PREFIX, fingerprint, AUTHORIZATION);

        cacheService.getIdentifiers(
                serviceUrl, OTHER_PREFIX, fingerprint, AUTHORIZATION);

        assertEquals(1, listRequests.get() - before);
    }

    @Test
    void shouldCacheHandleDetails() {

        String fingerprint = uniqueKey("details");
        String identifier = PREFIX + "/A";

        int before = detailRequests.get();

        var first = cacheService.getDetails(
                serviceUrl, identifier, fingerprint,
                AUTHORIZATION, PREFIX, "A");

        var second = cacheService.getDetails(
                serviceUrl, identifier, fingerprint,
                AUTHORIZATION, PREFIX, "A");

        assertEquals(identifier, first.getHandle());
        assertEquals(first.getHandle(), second.getHandle());

        assertEquals(1, detailRequests.get() - before);
    }

    @Test
    void shouldInvalidateOnlyUpdatedHandle() {

        String fingerprint = uniqueKey("update");

        String handleA = PREFIX + "/A";
        String handleB = PREFIX + "/B";

        cacheService.getDetails(
                serviceUrl, handleA, fingerprint,
                AUTHORIZATION, PREFIX, "A");

        cacheService.getDetails(
                serviceUrl, handleB, fingerprint,
                AUTHORIZATION, PREFIX, "B");

        int before = detailRequests.get();

        cacheService.invalidateDetails(
                serviceUrl, handleA, fingerprint);

        cacheService.getDetails(
                serviceUrl, handleA, fingerprint,
                AUTHORIZATION, PREFIX, "A");

        cacheService.getDetails(
                serviceUrl, handleB, fingerprint,
                AUTHORIZATION, PREFIX, "B");

        // A was refetched, B remained cached.
        assertEquals(1, detailRequests.get() - before);
    }

    @Test
    void shouldNotShareCacheAcrossCredentials() {

        String handle = PREFIX + "/A";

        String fingerprintA = uniqueKey("admin-a");
        String fingerprintB = uniqueKey("admin-b");

        int before = detailRequests.get();

        cacheService.getDetails(
                serviceUrl, handle, fingerprintA,
                AUTHORIZATION, PREFIX, "A");

        cacheService.getDetails(
                serviceUrl, handle, fingerprintB,
                AUTHORIZATION, PREFIX, "A");

        // Separate credentials = separate entries.
        assertEquals(2, detailRequests.get() - before);
    }

    @Test
    void shouldRefreshMetadataAfterInvalidation() {

        String fingerprint = uniqueKey("refresh");
        String identifier = PREFIX + "/UPDATED";

        metadataValue = "original";

        var original = cacheService.getDetails(
                serviceUrl, identifier, fingerprint,
                AUTHORIZATION, PREFIX, "UPDATED");

        assertEquals(
                "original",
                original.getValues().get(0).getData().getValue()
        );

        // Simulate a modification on the Handle Server.
        metadataValue = "updated";

        // Without invalidation, the old value is returned.
        var cached = cacheService.getDetails(
                serviceUrl, identifier, fingerprint,
                AUTHORIZATION, PREFIX, "UPDATED");

        assertEquals(
                "original",
                cached.getValues().get(0).getData().getValue()
        );

        // Invalidate only this handle.
        cacheService.invalidateDetails(
                serviceUrl, identifier, fingerprint);

        var refreshed = cacheService.getDetails(
                serviceUrl, identifier, fingerprint,
                AUTHORIZATION, PREFIX, "UPDATED");

        assertEquals(
                "updated",
                refreshed.getValues().get(0).getData().getValue()
        );
    }
}
