package site.wanjiahao.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

// 自定义校验注解
public class ListValConstraintValidator implements ConstraintValidator<ListVal, Integer> {

    private final Set<Integer> vals = new HashSet<>();

    // 初始化注解
    @Override
    public void initialize(ListVal constraintAnnotation) {
        int[] values = constraintAnnotation.value();
        for (int i = 0; i < values.length; i++) {
            vals.add(values[i]);
        }
    }

    // 判断是否校验成功
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return vals.contains(value);
    }
}
