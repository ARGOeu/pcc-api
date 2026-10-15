package gr.grnet.pccapi.validator.constraints;


import gr.grnet.pccapi.validator.validators.ContactTypeValidator;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@RegisterForReflection
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContactTypeValidator.class)
public @interface ValidContactType {
    String message() default "Invalid contact type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

