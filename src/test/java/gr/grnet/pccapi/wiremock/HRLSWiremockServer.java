package gr.grnet.pccapi.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Collections;
import java.util.Map;

public class HRLSWiremockServer implements QuarkusTestResourceLifecycleManager {

  private WireMockServer wireMockServer;

  @Override
  public Map<String, String> start() {

    wireMockServer = new WireMockServer(3490);
    wireMockServer.start();

    wireMockServer.stubFor(
        get(urlPathEqualTo("/hrls/handles"))
            .withQueryParams(
                Map.of(
                    "limit", equalTo("20"),
                    "page", equalTo("1"),
                    "URL", equalTo("domain.com"),
                    "EMAIL", equalTo("me@mail.com"),
                    "retrieverecords", equalTo("true")))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBodyFile("json/ServiceLookupResponse_HRLS.json")));

    wireMockServer.stubFor(
        get(urlPathEqualTo("/hrls/handles"))
            .withQueryParams(
                Map.of(
                    "URL", equalTo("domain.com"),
                    "EMAIL", equalTo("me@mail.com"),
                    "CHECKSUM", equalTo("f7b24993990ba4fc2104b09b63e7f975"),
                    "retrieverecords", equalTo("true")))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBodyFile("json/ServiceLookupResponse_HRLS_checksum.json")));

    wireMockServer.stubFor(
        get(urlPathEqualTo("/hrls/handles"))
            .withQueryParams(
                Map.of(
                    "limit", equalTo("10"),
                    "page", equalTo("0"),
                    "URL", equalTo("domain.com"),
                    "EMAIL", equalTo("me@mail.com"),
                    "retrieverecords", equalTo("false")))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBodyFile("json/ServiceLookupResponseFlat_HRLS.json")));

    wireMockServer.stubFor(
        get(urlPathEqualTo("/hrls/handles"))
            .withQueryParams(
                Map.of(
                    "URL", equalTo("invalid"),
                    "retrieverecords", equalTo("false")))
            .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

    return Collections.singletonMap(
        "quarkus.rest-client.\"gr.grnet.pccapi.client.hrls.HRLSClient\".url",
        wireMockServer.baseUrl());
  }

  @Override
  public void stop() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }
}
