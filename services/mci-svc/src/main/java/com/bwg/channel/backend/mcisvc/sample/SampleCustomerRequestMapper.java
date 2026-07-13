package com.bwg.channel.backend.mcisvc.sample;

import com.bwg.channel.backend.mcicommon.domain.MciRequest;
import com.bwg.channel.backend.mcicommon.domain.MciResponse;
import com.bwg.channel.backend.mcicommon.registry.TransactionDefinition;
import com.bwg.channel.backend.mcicommon.spi.MciMapper;
import org.springframework.stereotype.Component;

/**
 * 샘플 요청 mapper.
 * <p>
 * 실제 고객사에서는 표준 payload를 고객사 전문 필드명, 고정 길이 전문, XML, JSON 등으로 변환한다.
 */
@Component
public class SampleCustomerRequestMapper implements MciMapper {

    /** YAML transaction.requestMapper와 일치해야 하는 mapper 이름. */
    private static final String MAPPER_NAME = "sampleCustomerRequestMapper";

    @Override
    public String mapperName() {
        return MAPPER_NAME;
    }

    /** 샘플에서는 표준 요청과 대상 시스템 요청을 동일하게 취급한다. */
    @Override
    public MciRequest<?> toTargetRequest(MciRequest<?> standardRequest, TransactionDefinition definition) {
        return standardRequest;
    }

    /** 요청 mapper는 응답 변환에 관여하지 않으므로 샘플에서는 그대로 반환한다. */
    @Override
    public MciResponse<?> toStandardResponse(MciResponse<?> targetResponse, TransactionDefinition definition) {
        return targetResponse;
    }
}
