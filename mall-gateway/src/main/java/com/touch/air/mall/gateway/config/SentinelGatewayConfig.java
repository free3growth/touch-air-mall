//package com.touch.air.mall.gateway.config;
//
//import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
//import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
//import com.alibaba.fastjson.JSON;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author: bin.wang
// * @date: 2021/3/12 14:05
// */
//@Configuration
//public class SentinelGatewayConfig {
//
//    public SentinelGatewayConfig(){
//        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
//            //网关限流触发
//            @Override
//            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
//                Map<Integer, String> map = new HashMap<>();
//                map.put(400, "网关限流触发");
//                Mono<ServerResponse> body = ServerResponse.ok().body(Mono.just(JSON.toJSONString(map)), String.class);
//                return body;
//            }
//        });
//    }
//}
