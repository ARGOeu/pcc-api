package gr.grnet.pccapi.client.hrls;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(QueryParamBuildingFilter.class)
@ClientHeaderParam(name = "Authorization", value = "{basicAuth}")
@Path("/hrls/handles")
@RegisterRestClient
public interface HRLSClient {

  default String basicAuth() {
    return "Basic "
        + Base64.getEncoder()
            .encodeToString(
                String.format(
                        "%s:%s", System.getenv("HRLS_USERNAME"), System.getenv("HRLS_PASSWORD"))
                    .getBytes());
  }

  @GET
  Set<String> getHandlesFlat(Map<String, String> queryParamMap);

  @GET
  Map<String, List<HRLSHandle>> getHandles(Map<String, String> queryParamMap);
}
