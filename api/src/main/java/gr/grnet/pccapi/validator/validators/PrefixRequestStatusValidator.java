package gr.grnet.pccapi.validator.validators;

import gr.grnet.pccapi.enums.PrefixRequestStatus;
import gr.grnet.pccapi.validator.constraints.ValidPrefixRequestStatus;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

@RegisterForReflection
public class PrefixRequestStatusValidator implements ConstraintValidator<ValidPrefixRequestStatus, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null||value.equals("")) return true;
        return Arrays.stream(PrefixRequestStatus.values())
                .anyMatch(e -> e.name().equals(value));
    }
}

