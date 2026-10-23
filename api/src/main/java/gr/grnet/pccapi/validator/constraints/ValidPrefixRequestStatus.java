package gr.grnet.pccapi.validator.constraints;


import gr.grnet.pccapi.enums.PrefixRequestStatus;
import gr.grnet.pccapi.validator.validators.ContactTypeValidator;
import gr.grnet.pccapi.validator.validators.PrefixRequestStatusValidator;
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
@Constraint(validatedBy = PrefixRequestStatusValidator.class)
public @interface ValidPrefixRequestStatus {
    String message() default "Invalid prefix request status type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}