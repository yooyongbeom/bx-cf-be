package com.bwg.channel.backend.mcisvc.registry;

import com.bwg.channel.backend.mcicommon.registry.TransactionDefinition;
import com.bwg.channel.backend.mcicommon.registry.TransactionRegistry;
import com.bwg.channel.backend.mcisvc.config.MciTransactionProperties;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * YAML 설정을 사용하는 거래 registry 구현체.
 * <p>
 * 현재 단계에서는 제품 스타터킷의 기본 구현으로 사용하고, 추후 system-svc 관리자 API/DB 기반 registry가 생기면
 * 이 구현을 대체하거나 profile별로 선택한다.
 */
@Component
public class YamlTransactionRegistry implements TransactionRegistry {

    /** 거래 코드를 key로 변환해 router 조회 비용을 낮춘다. */
    private final Map<String, TransactionDefinition> transactions;

    public YamlTransactionRegistry(MciTransactionProperties properties) {
        // YAML은 애플리케이션 기동 시점에 한 번 바인딩되므로, 런타임 조회는 Map으로 단순화한다.
        // 추후 DB 기반 registry로 바꾸면 이 생성 시점 캐싱 전략도 refresh/cache 정책으로 확장한다.
        this.transactions = properties.getTransactions().stream()
                .collect(Collectors.toMap(TransactionDefinition::getCode, Function.identity()));
    }

    /** YAML에 등록된 거래 정의를 거래 코드로 조회한다. */
    @Override
    public Optional<TransactionDefinition> findByCode(String code) {
        return Optional.ofNullable(transactions.get(code));
    }
}
