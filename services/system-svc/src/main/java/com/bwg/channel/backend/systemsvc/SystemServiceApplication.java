package com.bwg.channel.backend.systemsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 기준정보(system-svc) Spring Boot 애플리케이션 진입점.
 */
@SpringBootApplication(
    // system-svc가 실제로 필요로 하는 런타임 패키지만 스캔한다.
    // security-common(JwtUtil 등)은 InternalAuthHeaders 상수만 컴파일 타임에 쓰이고
    // 런타임에는 불필요하므로 스캔 대상에서 제외한다(IDE에서 compileOnly가 런타임에 올라와도 안전).
    scanBasePackages = {
        "com.bwg.channel.backend.systemsvc",
        "com.bwg.channel.backend.common",
        "com.bwg.channel.backend.businesscommon"
    }
)
public class SystemServiceApplication {

    /**
     * system-svc 애플리케이션을 실행한다.
     */
    public static void main(String[] args) {
        SpringApplication.run(SystemServiceApplication.class, args);
    }
}
