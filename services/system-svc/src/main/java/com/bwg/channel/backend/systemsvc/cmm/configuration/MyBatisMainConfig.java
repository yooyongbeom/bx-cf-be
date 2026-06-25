package com.bwg.channel.backend.systemsvc.cmm.configuration;

import com.bwg.channel.backend.common.configuration.MyBatisMainConfigBase;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * system-svc MyBatis Mapper 스캔과 공통 MyBatis 설정을 연결한다.
 */
@Configuration
@MapperScan(
    basePackages = "com.bwg.channel.backend.systemsvc.repository.mybatis.mapper",
    sqlSessionFactoryRef = "mybatisMainSqlSessionFactory"
)
public class MyBatisMainConfig extends MyBatisMainConfigBase {
}
