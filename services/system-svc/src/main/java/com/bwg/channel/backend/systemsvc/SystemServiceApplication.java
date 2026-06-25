package com.bwg.channel.backend.systemsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 기준정보(system-svc) Spring Boot 애플리케이션 진입점.
 */
@SpringBootApplication(
    scanBasePackages = {"com.bwg.channel.backend.*"}
)
public class SystemServiceApplication {

    /**
     * system-svc 애플리케이션을 실행한다.
     */
    public static void main(String[] args) {
        SpringApplication.run(SystemServiceApplication.class, args);
    }
}
