package com.bwg.channel.backend.mcisvc.config;

import com.bwg.channel.backend.mcicommon.registry.TransactionDefinition;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * YAML 기반 MCI 거래 설정.
 * <p>
 * 초기 버전에서는 빠르게 제품 스타터킷으로 검증할 수 있도록 YAML을 사용한다. 운영 관리자 기능이 붙으면 동일한
 * TransactionDefinition을 DB에서 관리하는 registry로 전환한다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "mci")
public class MciTransactionProperties {

    /** mci.transactions 하위에 선언한 거래 정의 목록. */
    private List<TransactionDefinition> transactions = new ArrayList<>();
}
