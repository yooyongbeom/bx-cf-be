package com.bwg.channel.backend.authsvc.cmm.configuration;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(
    basePackages = "com.bwg.channel.backend.authsvc.repository.mybatis.mapper", // 이 DataSource를 사용할 Mapper 경로
    sqlSessionFactoryRef = "mybatisMainSqlSessionFactory"                       // 아래에서 정의할 SqlSessionFactory Bean 이름
)
public class MyBatisMainConfig {

    @Bean(name = "mybatisMainDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.mybatis-main")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "mybatisMainSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("mybatisMainDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath*:mappers/**/*.xml")
        );
        return sessionFactory.getObject();
    }

    @Bean(name = "mybatisMainTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("mybatisMainDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "mybatisMainSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("mybatisMainSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
