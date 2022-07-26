package gr.grnet.pccapi.exception;

import gr.grnet.pccapi.dto.APIResponseMsg;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Exception mapper that handles all the api exceptions and produces the appropriate api response.
 */
@Provider
public class APIExceptionMapper implements ExceptionMapper<ClientErrorException> {

  @Override
  public Response toResponse(ClientErrorException e) {
    return Response.status(e.getResponse().getStatus())
        .entity(new APIResponseMsg(e.getMessage()))
        .build();
  }
}
