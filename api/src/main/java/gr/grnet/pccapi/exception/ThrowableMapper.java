package gr.grnet.pccapi.exception;

import gr.grnet.pccapi.dto.APIResponseMsg;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * ThrowableMapper handles any unexpected exceptions that might occur and returns an appropriate 500
 * response.
 */
@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {

  @Override
  public Response toResponse(Throwable e) {
    e.printStackTrace();
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(new APIResponseMsg("INTERNAL_SERVER_ERROR"))
        .build();
  }
}
