package com.bwg.channel.backend.mcisvc.sample;

import com.bwg.channel.backend.mcicommon.domain.MciRequest;
import com.bwg.channel.backend.mcicommon.domain.MciResponse;
import com.bwg.channel.backend.mcicommon.registry.TransactionDefinition;
import com.bwg.channel.backend.mcicommon.spi.MciMapper;
import org.springframework.stereotype.Component;

/**
 * 샘플 응답 mapper.
 * <p>
 * 실제 고객사에서는 고객사 응답 코드/메시지/본문을 표준 MciResponse로 변환하고, 오류 코드 매핑이나 데이터 마스킹도
 * 이 단계에서 다룬다.
 */
@Component
public class SampleCustomerResponseMapper implements MciMapper {

    /** YAML transaction.responseMapper와 일치해야 하는 mapper 이름. */
    private static final String MAPPER_NAME = "sampleCustomerResponseMapper";

    @Override
    public String mapperName() {
        return MAPPER_NAME;
    }

    /** 응답 mapper는 요청 변환에 관여하지 않으므로 샘플에서는 그대로 반환한다. */
    @Override
    public MciRequest<?> toTargetRequest(MciRequest<?> standardRequest, TransactionDefinition definition) {
        return standardRequest;
    }

    /** 샘플에서는 대상 시스템 응답과 표준 응답을 동일하게 취급한다. */
    @Override
    public MciResponse<?> toStandardResponse(MciResponse<?> targetResponse, TransactionDefinition definition) {
        return targetResponse;
    }
}
