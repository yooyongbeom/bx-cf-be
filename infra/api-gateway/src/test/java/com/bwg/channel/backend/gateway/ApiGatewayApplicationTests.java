package com.bwg.channel.backend.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
@SpringBootTest(properties = {
        "spring.jwt.secret=01234567890123456789012345678901",
        "eureka.client.enabled=false",
        "app.log-path=build/logs"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }

}
