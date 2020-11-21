package site.wanjiahao.gulimall.product.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.wanjiahao.common.code.BizCodeEnum;
import site.wanjiahao.common.utils.R;

import java.util.HashMap;

@Slf4j
@RestControllerAdvice(basePackages = "site.wanjiahao.gulimall.product.controller")
public class HandleControllerException {

    // 方法参数绑定异常
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("感知到异常{}", e.toString());
        BindingResult bindingResult = e.getBindingResult();
        HashMap<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors()
                .forEach((item) -> {
                    String field = item.getField();
                    String defaultMessage = item.getDefaultMessage();
                    errorMap.put(field, defaultMessage);
                });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getBizCode(),
                BizCodeEnum.VALID_EXCEPTION.getMessage()).put("data", errorMap);
    }

    /**
     * 任意异常
     *  Throwable
     *      Error
     *          1.StackOverflowException
     *          2.OutOfMemoryException
     *      Exception
     *          运行异常 (RuntimeException)
     *              1.NullPointException
     *              2.ArrayIndexOutOfBoundException
     *          非运行异常
     *              1.IOException
     *
     */
    /*@ExceptionHandler(value = Throwable.class)
    public R handleThrowable(Throwable throwable) {
        log.error("感知异常{}", throwable.toString());
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getBizCode(),
                BizCodeEnum.UNKNOWN_EXCEPTION.getMessage());
    }*/
}
