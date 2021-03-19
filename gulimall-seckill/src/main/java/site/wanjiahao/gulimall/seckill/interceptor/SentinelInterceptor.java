package site.wanjiahao.gulimall.seckill.interceptor;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;
import site.wanjiahao.common.utils.R;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class SentinelInterceptor implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
        R error = R.error(502, "太多请求");
        httpServletResponse.setContentType("application/json; charset=utf-8");
        httpServletResponse.getWriter().write(JSON.toJSONString(error));
    }
}
