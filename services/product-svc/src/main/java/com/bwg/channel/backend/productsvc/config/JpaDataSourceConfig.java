package com.bwg.channel.backend.productsvc.config;

import com.bwg.channel.backend.common.config.JpaMainConfigBase;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * product-svc JPA Repository 스캔과 Entity 패키지 설정
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.bwg.channel.backend.productsvc.repository.jpa",
    entityManagerFactoryRef = "jpaMainEntityManagerFactory",
    transactionManagerRef = "jpaMainTransactionManager"
)
public class JpaDataSourceConfig extends JpaMainConfigBase {

    /**
     * 상품 Entity 패키지 경로
     */
    @Override
    protected String entityPackagesToScan() {
        return "com.bwg.channel.backend.productsvc.domain.entity";
    }
}
