package site.wanjiahao.common.valid;

import site.wanjiahao.common.valid.ConstraintValidator.CheckKgConstraintValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {CheckKgConstraintValidator.class})
public @interface CheckKg {

    String message() default "{javax.validation.constraints.CheckKg.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
