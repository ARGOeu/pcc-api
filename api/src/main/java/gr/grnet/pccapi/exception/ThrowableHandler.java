package gr.grnet.pccapi.exception;

import gr.grnet.pccapi.dto.InformativeResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class ThrowableHandler implements ExceptionMapper<Throwable> {

  private static final Logger LOG = Logger.getLogger(ThrowableHandler.class);

  @Override
  public Response toResponse(Throwable e) {

    LOG.error("Internal Server Error", e);

    var response = new InformativeResponse();
    response.code = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    response.message = "An internal server error occurred.";

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(response)
            .build();
  }
}