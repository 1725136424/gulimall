package site.wanjiahao.gulimall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import site.wanjiahao.common.utils.R;

@Configuration
public class SentinelGatewayConfig {

    public SentinelGatewayConfig() {
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            // 网关限流的回调
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                R error = R.error(521, "网关限流");
                return ServerResponse.ok().body(Mono.just(error), R.class);
            }
        });
    }
}
