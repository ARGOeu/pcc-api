package gr.grnet.pccapi.exception;

import lombok.Getter;

@Getter
public class HandleServiceException extends RuntimeException {

    private final int status;

    public HandleServiceException(int status, String message) {
        super(message);
        this.status = status;
    }
}