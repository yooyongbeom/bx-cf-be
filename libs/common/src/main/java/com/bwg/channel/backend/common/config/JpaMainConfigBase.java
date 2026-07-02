package com.bwg.channel.backend.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

public abstract class JpaMainConfigBase {

    /** Entity 패키지 경로 — 각 서비스에서 반환 */
    protected abstract String entityPackagesToScan();

    /** Hibernate dialect — 기본값 PostgreSQL, 서비스에서 필요시 오버라이드 */
    protected String hibernateDialect() {
        return "org.hibernate.dialect.PostgreSQLDialect";
    }

    /** DDL auto — 기본값 update, 서비스에서 필요시 오버라이드 */
    protected String ddlAuto() {
        return "update";
    }

    @Bean(name = "originalJpaMainDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.jpa-main")
    public DataSource originalDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "jpaMainDataSource")
    public DataSource dataSource(@Qualifier("originalJpaMainDataSource") DataSource originalDataSource) {
        return originalDataSource;
    }

    @Primary
    @Bean(name = "jpaMainEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("jpaMainDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(entityPackagesToScan());
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto());
        properties.put("hibernate.dialect", hibernateDialect());
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
