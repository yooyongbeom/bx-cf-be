package com.bwg.channel.backend.mcisvc;

import com.bwg.channel.backend.mcisvc.config.MciTransactionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * MCI 서비스 Spring Boot 진입점.
 * <p>
 * 금융 채널의 공통 라우팅 기능은 mci-common을 사용하고, 이 서비스는 설정 로딩과 실제 adapter/mapper Bean 구성을 담당한다.
 */
@SpringBootApplication(
        scanBasePackages = {"com.bwg.channel.backend.*"}
)
@EnableConfigurationProperties({
        MciTransactionProperties.class
})
public class MciServiceApplication {

    /** mci-svc 애플리케이션을 기동한다. */
    public static void main(String[] args) {
        SpringApplication.run(MciServiceApplication.class, args);
    }
}
