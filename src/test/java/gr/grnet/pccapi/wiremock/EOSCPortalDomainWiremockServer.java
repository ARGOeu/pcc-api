package gr.grnet.pccapi.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Collections;
import java.util.Map;

public class EOSCPortalDomainWiremockServer implements QuarkusTestResourceLifecycleManager {

  private WireMockServer wireMockServer;

  @Override
  public Map<String, String> start() {

    wireMockServer = new WireMockServer(3467);
    wireMockServer.start();

    wireMockServer.stubFor(
        get(urlEqualTo("/vocabulary/byType/SCIENTIFIC_DOMAIN"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBodyFile("json/DomainResponse_EOSC.json")));

    return Collections.singletonMap(
        "quarkus.rest-client.\"gr.grnet.pccapi.client.EOSCPortalClient\".url",
        wireMockServer.baseUrl());
  }

  @Override
  public void stop() {
    if (null != wireMockServer) {
      wireMockServer.stop();
    }
  }
}
