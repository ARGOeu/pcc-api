package gr.grnet.pccapi;

import io.quarkus.test.junit.QuarkusTestProfile;

public class PCCApiTestProfile implements QuarkusTestProfile {

  @Override
  public boolean disableApplicationLifecycleObservers() {
    return true;
  }
}
