package com.bwg.channel.backend.productsvc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
@SpringBootTest(properties = {
        "spring.jwt.secret=01234567890123456789012345678901",
        "spring.datasource.jpa-main.jdbc-url=jdbc:h2:mem:product_jpa;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.mybatis-main.jdbc-url=jdbc:h2:mem:product_mybatis;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.jpa-main.driver-class-name=org.h2.Driver",
        "spring.datasource.mybatis-main.driver-class-name=org.h2.Driver",
        "spring.datasource.jpa-main.username=sa",
        "spring.datasource.mybatis-main.username=sa",
        "spring.datasource.jpa-main.password=",
        "spring.datasource.mybatis-main.password=",
        "eureka.client.enabled=false",
        "app.log-path=build/logs"
})
class ProductServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
