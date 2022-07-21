package gr.grnet.pccapi.exception;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

/**
 * ConflictException is used to describe errors that occur
 * when a resource is trying to use information that has already been used by another resource in the service.
 */
public class ConflictException extends ClientErrorException {
    public ConflictException(String message) {
        super(message, Response.Status.CONFLICT);
    }
}