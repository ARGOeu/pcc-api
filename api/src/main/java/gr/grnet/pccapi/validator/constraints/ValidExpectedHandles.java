package gr.grnet.pccapi.validator.constraints;


import gr.grnet.pccapi.validator.validators.ContactTypeValidator;
import gr.grnet.pccapi.validator.validators.ExpectedHandlesValidator;
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
@Constraint(validatedBy = ExpectedHandlesValidator.class)
public @interface ValidExpectedHandles {
    String message() default "Invalid expected handles type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}