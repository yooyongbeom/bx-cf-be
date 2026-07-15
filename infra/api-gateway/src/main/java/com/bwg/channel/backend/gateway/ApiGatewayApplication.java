package com.bwg.channel.backend.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.bwg.channel.backend.gateway.*"
        ,"com.bwg.channel.backend.securitycommon.*"
        ,"com.bwg.channel.backend.sessioncontext.*"   // 세션 존재 검증(SessionContextService/Redis 설정) 스캔
})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
