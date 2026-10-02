package gr.grnet.pccapi.validator.constraints;
import jakarta.validation.ValidationException;
import lombok.Getter;

@Getter
public class CustomValidationException extends ValidationException {

    private int code;

    public CustomValidationException(String message, int code){
        super(message);
        this.code = code;
    }

}
