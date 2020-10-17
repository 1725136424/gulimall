package site.wanjiahao.common.valid.ConstraintValidator;

import site.wanjiahao.common.utils.DecimalUtil;
import site.wanjiahao.common.valid.CheckMoney;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class CheckMoneyConstraintValidator implements ConstraintValidator<CheckMoney, BigDecimal> {

    @Override
    public void initialize(CheckMoney constraintAnnotation) {
        // 初始化数据
    }

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        return DecimalUtil.judgeTwoDecimal(value);
    }
}
