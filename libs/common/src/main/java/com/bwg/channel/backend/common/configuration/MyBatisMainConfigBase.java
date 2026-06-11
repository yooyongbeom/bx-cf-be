package com.bwg.channel.backend.common.configuration;

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

import javax.sql.DataSource;

public abstract class MyBatisMainConfigBase {

    /** mapper XML 위치 — 기본값 classpath*:mappers/**\/*.xml, 필요시 오버라이드 */
    protected String mapperLocations() {
        return "classpath*:mappers/**/*.xml";
    }

    @Bean(name = "mybatisMainDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.mybatis-main")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "mybatisMainSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("mybatisMainDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources(mapperLocations())
        );
        return sessionFactory.getObject();
    }

    @Bean(name = "mybatisMainTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("mybatisMainDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "mybatisMainSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(
            @Qualifier("mybatisMainSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
