package gr.grnet.pccapi.validator.constraints;

import gr.grnet.pccapi.repository.Repository;
import gr.grnet.pccapi.validator.validators.NotFoundEntityValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotFoundEntityValidator.class)
@Documented
public @interface NotFoundEntity {

    String message() default "Not founded:";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<? extends Repository<?,?>> repository();
}
