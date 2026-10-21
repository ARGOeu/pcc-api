package gr.grnet.pccapi.validator.validators;

import gr.grnet.pccapi.enums.PrefixExpectedHandles;
import gr.grnet.pccapi.validator.constraints.ValidExpectedHandles;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

@RegisterForReflection
public class ExpectedHandlesValidator implements ConstraintValidator<ValidExpectedHandles, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null||value.equals("")) return true;
        return Arrays.stream(PrefixExpectedHandles.values())
                .anyMatch(e -> e.name().equals(value));
    }
}

