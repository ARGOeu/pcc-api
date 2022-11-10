package gr.grnet.pccapi.client.hrls;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
                        "%s:%s", "pcc", "142505d97c61c7013a82ef6b70788e306695b59b1bce386285111df0b068edfd")
                    .getBytes());
  }

  @GET
  Set<String> getHandlesFlat(Map<String, String> queryParamMap);

  @GET
  Map<String, List<HRLSHandle>> getHandles(Map<String, String> queryParamMap);
}
