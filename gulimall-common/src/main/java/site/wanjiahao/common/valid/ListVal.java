package site.wanjiahao.common.valid;

import site.wanjiahao.common.valid.ConstraintValidator.ListValConstraintValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { ListValConstraintValidator.class})
public @interface ListVal {

    String message() default "{javax.validation.constraints.ListVal.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    // 指定值的数组
    int[] value() default {};

}
