package com.bwg.channel.backend.mcicommon.registry;

import java.util.Optional;

/**
 * 거래 정의 조회 SPI.
 * <p>
 * YAML, DB, 캐시 등 저장 위치가 바뀌어도 router는 이 인터페이스만 바라본다.
 */
@FunctionalInterface
public interface TransactionRegistry {

    /** 거래 코드로 활성/비활성 포함 거래 정의를 조회한다. */
    Optional<TransactionDefinition> findByCode(String code);
}
