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

    /** product-svc Mapper XML에서 사용할 DTO alias scan 패키지 */
    @Override
    protected String typeAliasesPackage() {
        return "com.bwg.channel.backend.common.domain.dto,"
                + "com.bwg.channel.backend.productsvc.domain.dto";
    }
}
