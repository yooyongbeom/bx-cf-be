package com.bwg.channel.backend.systemsvc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
@SpringBootTest(properties = {
        "spring.datasource.mybatis-main.jdbc-url=jdbc:h2:mem:system_mybatis;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.mybatis-main.driver-class-name=org.h2.Driver",
        "spring.datasource.mybatis-main.username=sa",
        "spring.datasource.mybatis-main.password=",
        "eureka.client.enabled=false",
        "app.log-path=build/logs"
})
class SystemServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
