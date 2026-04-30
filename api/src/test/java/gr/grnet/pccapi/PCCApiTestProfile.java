package gr.grnet.pccapi;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class PCCApiTestProfile implements QuarkusTestProfile {

  @Override
  public boolean disableApplicationLifecycleObservers() {
    return true;
  }

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of(
        "quarkus.flyway.schemas",
        "quarkus",
        "quarkus.datasource.username",
        "quarkus",
        "quarkus.datasource.password",
        "quarkus");
  }
}
