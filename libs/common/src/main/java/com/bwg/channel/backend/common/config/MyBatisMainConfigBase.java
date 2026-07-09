package com.bwg.channel.backend.common.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * MyBatis 기반 서비스가 공통으로 상속하는 main datasource 설정 베이스
 * <p>
 * mapper XML 위치와 type alias package만 서비스별로 오버라이드하고,
 * datasource/sqlSessionFactory/sqlSessionTemplate/transactionManager 빈 이름은 공통 규칙을 따른다.
 */
public abstract class MyBatisMainConfigBase {

    /** mapper XML 위치 — 기본값 classpath*:mappers/**\/*.xml, 필요시 오버라이드 */
    protected String mapperLocations() {
        return "classpath*:mappers/**/*.xml";
    }

    /** MyBatis type alias scan package. 필요한 서비스 설정에서 오버라이드한다. */
    protected String typeAliasesPackage() {
        return null;
    }

    @Bean(name = "mybatisMainDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.mybatis-main")
    public DataSource dataSource() {
        // spring.datasource.mybatis-main.* 설정을 MyBatis 전용 datasource로 바인딩한다.
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "mybatisMainSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("mybatisMainDataSource") DataSource dataSource) throws Exception {
        // mapper XML과 선택적 type alias package를 묶어 SqlSessionFactory를 구성한다.
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources(mapperLocations())
        );
        if (StringUtils.hasText(typeAliasesPackage())) {
            // type alias package가 비어 있으면 MyBatis 기본 동작을 유지한다.
            sessionFactory.setTypeAliasesPackage(typeAliasesPackage());
        }
        return sessionFactory.getObject();
    }

    @Bean(name = "mybatisMainTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("mybatisMainDataSource") DataSource dataSource) {
        // MyBatis datasource 기준의 로컬 트랜잭션 매니저.
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "mybatisMainSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(
            @Qualifier("mybatisMainSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        // Mapper 인터페이스가 thread-safe하게 사용할 SqlSessionTemplate.
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
