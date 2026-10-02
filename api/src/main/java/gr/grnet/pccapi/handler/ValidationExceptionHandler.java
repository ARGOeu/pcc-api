package gr.grnet.pccapi.handler;

import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.validator.constraints.CustomValidationException;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class ValidationExceptionHandler implements ExceptionMapper<ValidationException> {

    private static final Logger LOG = Logger.getLogger(ValidationExceptionHandler.class);

    @Override
    public Response toResponse(ValidationException e) {

        LOG.error("Validation Error", e);

        var response = new InformativeResponse();

        if(e instanceof ResteasyReactiveViolationException){

            response.message = ((ResteasyReactiveViolationException) e).getConstraintViolations().stream().findFirst().get().getMessageTemplate();
            response.code = Response.Status.BAD_REQUEST.getStatusCode();
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        } else if(e.getCause() instanceof CustomValidationException){

            var exception = (CustomValidationException) e.getCause();
            response.message = exception.getMessage();
            response.code = exception.getCode();
            return Response.status(exception.getCode()).entity(response).build();
        } else {

            response.message = e.getMessage();
            response.code = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }
    }
}

