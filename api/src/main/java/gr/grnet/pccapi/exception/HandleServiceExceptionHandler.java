package gr.grnet.pccapi.exception;

import gr.grnet.pccapi.dto.InformativeResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class HandleServiceExceptionHandler implements ExceptionMapper<HandleServiceException> {

    private static final Logger LOG =
            Logger.getLogger(HandleServiceExceptionHandler.class);

    @Override
    public Response toResponse(HandleServiceException e) {

        LOG.errorf(
                "Handle service operation failed with status %s: %s",
                e.getStatus(),
                e.getMessage());

        var response = new InformativeResponse();
        response.code = e.getStatus();
        response.message = e.getMessage();

        return Response.status(e.getStatus())
                .entity(response)
                .build();
    }
}