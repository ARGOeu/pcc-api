package gr.grnet.pccapi;

import io.restassured.RestAssured;

public class KeycloakTestClient {

    private static String TOKEN_URL;

    public static void init(String baseUrl) {
        TOKEN_URL = baseUrl + "/protocol/openid-connect/token";
    }

    public static String getAccessToken(String username) {
        return RestAssured
                .given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "frontend-service")
                .formParam("username", username)
                .formParam("password", username)
                .formParam("scope", "openid voperson_id email profile entitlements")
                .when()
                .post(TOKEN_URL)
                .then()
                .statusCode(200)
                .extract()
                .path("access_token");
    }
}