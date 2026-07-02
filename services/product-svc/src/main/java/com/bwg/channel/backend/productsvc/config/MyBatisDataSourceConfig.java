package com.bwg.channel.backend.productsvc.config;

import com.bwg.channel.backend.common.config.MyBatisMainConfigBase;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * product-svc MyBatis Mapper 스캔과 공통 MyBatis 설정 연결
 */
@Configuration
@MapperScan(
    basePackages = "com.bwg.channel.backend.productsvc.repository.mybatis.mapper",
    sqlSessionFactoryRef = "mybatisMainSqlSessionFactory"
)
public class MyBatisDataSourceConfig extends MyBatisMainConfigBase {
}
