package com.bwg.channel.backend.authsvc.config;

import com.bwg.channel.backend.common.config.MyBatisMainConfigBase;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(
    basePackages = "com.bwg.channel.backend.authsvc.repository.mybatis.mapper",
    sqlSessionFactoryRef = "mybatisMainSqlSessionFactory"
)
public class MyBatisDataSourceConfig extends MyBatisMainConfigBase {
}
