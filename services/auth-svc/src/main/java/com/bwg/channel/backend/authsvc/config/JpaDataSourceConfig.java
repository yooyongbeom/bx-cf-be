package com.bwg.channel.backend.authsvc.config;

import com.bwg.channel.backend.common.config.JpaMainConfigBase;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.bwg.channel.backend.authsvc.repository.jpa",
    entityManagerFactoryRef = "jpaMainEntityManagerFactory",
    transactionManagerRef = "jpaMainTransactionManager"
)
public class JpaDataSourceConfig extends JpaMainConfigBase {

    @Override
    protected String entityPackagesToScan() {
        return "com.bwg.channel.backend.authsvc.domain.entity";
    }
}
