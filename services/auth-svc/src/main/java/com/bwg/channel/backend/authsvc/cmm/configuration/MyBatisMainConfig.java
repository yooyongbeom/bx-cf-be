package com.bwg.channel.backend.authsvc.cmm.configuration;

import com.bwg.channel.backend.common.configuration.MyBatisMainConfigBase;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(
    basePackages = "com.bwg.channel.backend.authsvc.repository.mybatis.mapper",
    sqlSessionFactoryRef = "mybatisMainSqlSessionFactory"
)
public class MyBatisMainConfig extends MyBatisMainConfigBase {
}
