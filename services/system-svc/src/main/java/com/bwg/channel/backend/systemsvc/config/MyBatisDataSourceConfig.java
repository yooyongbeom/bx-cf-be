package com.bwg.channel.backend.systemsvc.config;

import com.bwg.channel.backend.common.config.MyBatisMainConfigBase;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * system-svc MyBatis Mapper 스캔과 공통 MyBatis 설정을 연결한다.
 */
@Configuration
@MapperScan(
    basePackages = {
        "com.bwg.channel.backend.systemsvc.commoncode.repository.mybatis.mapper",
        "com.bwg.channel.backend.systemsvc.menu.repository.mybatis.mapper"
    },
    sqlSessionFactoryRef = "mybatisMainSqlSessionFactory"
)
public class MyBatisDataSourceConfig extends MyBatisMainConfigBase {

    /** system-svc Mapper XML에서 사용할 DTO alias scan 패키지 */
    @Override
    protected String typeAliasesPackage() {
        return "com.bwg.channel.backend.common.domain.dto,"
                + "com.bwg.channel.backend.systemsvc.commoncode.dto,"
                + "com.bwg.channel.backend.systemsvc.menu.dto";
    }
}
