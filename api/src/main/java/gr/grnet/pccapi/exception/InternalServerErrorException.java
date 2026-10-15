package gr.grnet.pccapi.exception;

import jakarta.ws.rs.ServerErrorException;

public class InternalServerErrorException extends ServerErrorException {

    public InternalServerErrorException(String message, int status) {
        super(message, status);
    }
}

