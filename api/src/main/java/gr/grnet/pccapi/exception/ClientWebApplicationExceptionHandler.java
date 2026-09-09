package gr.grnet.pccapi.exception;

import gr.grnet.pccapi.dto.InformativeResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

@Provider
public class ClientWebApplicationExceptionHandler
        implements ExceptionMapper<ClientWebApplicationException> {

    private static final Logger LOG =
            Logger.getLogger(ClientWebApplicationExceptionHandler.class);

    @Override
    public Response toResponse(ClientWebApplicationException e) {

        var status = e.getResponse().getStatus();

        LOG.errorf(e, "External service request failed with status %s", status);

        var response = new InformativeResponse();
        response.code = status;
        response.message = "External service request failed.";

        return Response.status(status)
                .entity(response)
                .build();
    }
}