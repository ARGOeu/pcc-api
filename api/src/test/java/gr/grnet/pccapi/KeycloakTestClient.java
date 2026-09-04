package gr.grnet.pccapi;


import io.restassured.response.Response;
import io.restassured.RestAssured;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeycloakTestClient {

    private static final Map<String, String> tokenCache = new ConcurrentHashMap<>();

    private static String TOKEN_URL;

    public static void init(String baseUrl) {
        TOKEN_URL = baseUrl + "/protocol/openid-connect/token";
    }

    public static String getAccessToken(String username, String password) {
        var cacheKey = username + ":" + password;
        return tokenCache.computeIfAbsent(cacheKey, k -> fetchAccessToken(username, password));
    }

    private static String fetchAccessToken(String username, String password) {
        long deadline = System.currentTimeMillis() + 30_000;
        Exception lastError = null;

        while (System.currentTimeMillis() < deadline) {
            try {
                Response response = RestAssured
                        .given()
                        .contentType("application/x-www-form-urlencoded")
                        .formParam("grant_type", "password")
                        .formParam("client_id", "frontend-service")
                        .formParam("username", username)
                        .formParam("password", password)
                        .formParam("scope", "openid voperson_id email profile entitlements")
                        .when()
                        .post(TOKEN_URL);

                if (response.statusCode() == 200) {
                    return response.path("access_token");
                }

                lastError = new RuntimeException(
                        "Status " + response.statusCode() + ": " + response.getBody().asString());

            } catch (Exception e) {
                lastError = e;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ie);
            }
        }

        throw new RuntimeException(
                "Could not obtain Keycloak token for user '" + username + "' after 30s.", lastError);
    }
}
