package gr.grnet.pccapi.client.hrls;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Map;

/**
 * QueryParamBuildingFilter allows us to dynamically build the query parameters and as a result we
 * ignore any empty query parameters.The filter is only used with the {@link HRLSClient}.
 */
public class QueryParamBuildingFilter implements ClientRequestFilter {

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    UriBuilder uriBuilder = UriBuilder.fromUri(requestContext.getUri());
    Map<String, String> allParam = (Map<String, String>) requestContext.getEntity();
    for (String key : allParam.keySet()) {
      if (!allParam.get(key).isBlank()) {
        uriBuilder.queryParam(key, allParam.get(key));
      }
    }
    requestContext.setUri(uriBuilder.build());
    requestContext.setEntity(null);
  }
}
