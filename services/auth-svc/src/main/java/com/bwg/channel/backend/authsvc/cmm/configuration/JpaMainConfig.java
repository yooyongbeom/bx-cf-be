package com.bwg.channel.backend.authsvc.cmm.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.bwg.channel.backend.authsvc.repository.jpa", // 이 DataSource를 사용할 Repository 경로
    entityManagerFactoryRef = "jpaMainEntityManagerFactory",         // 아래에서 정의할 EntityManagerFactory Bean 이름
    transactionManagerRef = "jpaMainTransactionManager"              // 아래에서 정의할 TransactionManager Bean 이름
)
public class JpaMainConfig {
    @Bean(name = "originalJpaMainDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.jpa-main")
    public DataSource originalDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary // 여러 DataSource 중 기본으로 사용할 DataSource임을 명시
    @Bean(name = "jpaMainDataSource")
    public DataSource dataSource(@Qualifier("originalJpaMainDataSource") DataSource originalDataSource) {
        return originalDataSource;
    }

    @Primary
    @Bean(name = "jpaMainEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("jpaMainDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.bwg.channel.backend.authsvc.domain.entity"); // Entity 경로

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        // application.yml의 jpa.properties 설정을 여기서 읽어옴
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "create");
        // properties.put("hibernate.show_sql", "true"); // yml 설정으로 대체
        // properties.put("hibernate.format_sql", "true"); // yml 설정으로 대체
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");

        // Hibernate 6에서 쿼리 최적화를 위해 사용하는 설정
        properties.put("hibernate.format_sql", true);

        em.setJpaPropertyMap(properties);
        return em;
    }
    @Primary
    @Bean(name = "jpaMainTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("jpaMainEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}

