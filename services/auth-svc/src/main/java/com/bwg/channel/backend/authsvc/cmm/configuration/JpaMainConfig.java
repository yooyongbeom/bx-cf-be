package com.bwg.channel.backend.authsvc.cmm.configuration;

import com.bwg.channel.backend.common.configuration.JpaMainConfigBase;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.bwg.channel.backend.authsvc.repository.jpa",
    entityManagerFactoryRef = "jpaMainEntityManagerFactory",
    transactionManagerRef = "jpaMainTransactionManager"
)
public class JpaMainConfig extends JpaMainConfigBase {

    @Override
    protected String entityPackagesToScan() {
        return "com.bwg.channel.backend.authsvc.domain.entity";
    }
}
