package gr.grnet.pccapi.exception;

import gr.grnet.pccapi.dto.APIResponseMsg;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

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
