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

/**
 * JPA 기반 서비스가 공통으로 상속하는 main datasource 설정 베이스
 * <p>
 * 각 서비스 설정 클래스는 entity scan package만 제공하고, datasource/entityManager/transactionManager
 * 빈 이름은 이 클래스의 규칙을 따른다.
 */
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
        // spring.datasource.jpa-main.* 설정을 그대로 바인딩한 원본 datasource.
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "jpaMainDataSource")
    public DataSource dataSource(@Qualifier("originalJpaMainDataSource") DataSource originalDataSource) {
        // 서비스 내 기본 datasource로 주입되도록 @Primary 별칭 빈을 둔다.
        return originalDataSource;
    }

    @Primary
    @Bean(name = "jpaMainEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("jpaMainDataSource") DataSource dataSource) {
        // EntityManagerFactory는 서비스별 entity package와 공통 Hibernate 설정을 조합한다.
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
        // jpaMainEntityManagerFactory와 1:1로 묶인 transaction manager.
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
