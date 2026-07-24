package com.bwg.channel.backend.authsvc.config;

import com.bwg.channel.backend.common.config.MyBatisMainConfigBase;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(
    basePackages = {
        "com.bwg.channel.backend.authsvc.authentication.repository.mybatis.mapper",
        "com.bwg.channel.backend.authsvc.user.repository.mybatis.mapper"
    },
    sqlSessionFactoryRef = "mybatisMainSqlSessionFactory"
)
public class MyBatisDataSourceConfig extends MyBatisMainConfigBase {

    /** auth-svc Mapper XML에서 사용할 DTO alias scan 패키지 */
    @Override
    protected String typeAliasesPackage() {
        return "com.bwg.channel.backend.common.domain.dto,"
                + "com.bwg.channel.backend.authsvc.authentication.dto,"
                + "com.bwg.channel.backend.authsvc.user.dto";
    }
}
