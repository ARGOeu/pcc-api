package gr.grnet.pccapi.validator.validators;

import gr.grnet.pccapi.enums.ContactType;
import gr.grnet.pccapi.validator.constraints.ValidContactType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


import java.util.Arrays;

@RegisterForReflection
public class ContactTypeValidator implements ConstraintValidator<ValidContactType, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null||value.equals("")) return true;
        return Arrays.stream(ContactType.values())
                .anyMatch(e -> e.name().equals(value));
    }
}

