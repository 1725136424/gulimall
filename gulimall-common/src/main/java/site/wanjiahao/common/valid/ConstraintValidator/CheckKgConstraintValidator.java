package site.wanjiahao.common.valid.ConstraintValidator;

import site.wanjiahao.common.utils.DecimalUtil;
import site.wanjiahao.common.valid.CheckKg;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class CheckKgConstraintValidator implements ConstraintValidator<CheckKg, BigDecimal> {
    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        return DecimalUtil.judgeThreeDecimal(value);
    }
}
