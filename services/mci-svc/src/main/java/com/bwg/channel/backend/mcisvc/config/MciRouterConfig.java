package com.bwg.channel.backend.mcisvc.config;

import com.bwg.channel.backend.mcicommon.registry.TransactionRegistry;
import com.bwg.channel.backend.mcicommon.router.MciRouter;
import com.bwg.channel.backend.mcicommon.spi.MciAdapter;
import com.bwg.channel.backend.mcicommon.spi.MciMapper;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCI router 조립 설정.
 * <p>
 * 고객사별 adapter/mapper가 Bean으로 추가되면 Spring이 목록으로 주입하고, router가 transaction registry 이름 기준으로
 * 필요한 구현체를 선택한다.
 */
@Configuration
public class MciRouterConfig {

    /** YAML/DB registry와 adapter/mapper 목록을 연결해 MCI 실행 router를 만든다. */
    @Bean
    public MciRouter mciRouter(
            TransactionRegistry transactionRegistry,
            List<MciAdapter> adapters,
            List<MciMapper> mappers
    ) {
        return new MciRouter(transactionRegistry, adapters, mappers);
    }
}
