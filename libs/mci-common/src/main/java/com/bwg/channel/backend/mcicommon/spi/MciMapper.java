package com.bwg.channel.backend.mcicommon.spi;

import com.bwg.channel.backend.mcicommon.domain.MciRequest;
import com.bwg.channel.backend.mcicommon.domain.MciResponse;
import com.bwg.channel.backend.mcicommon.registry.TransactionDefinition;

/**
 * 표준 전문과 고객사 전문 사이의 변환 SPI.
 * <p>
 * 같은 거래라도 고객사마다 필드명, 포맷, 코드 체계가 다르므로 변환 책임을 mapper에 둔다.
 */
public interface MciMapper {

    /** transaction registry에서 참조하는 mapper 이름. */
    String mapperName();

    /** 채널/backend 표준 요청을 고객사 시스템 요청 구조로 변환한다. */
    MciRequest<?> toTargetRequest(MciRequest<?> standardRequest, TransactionDefinition definition);

    /** 고객사 시스템 응답을 채널/backend 표준 응답 구조로 변환한다. */
    MciResponse<?> toStandardResponse(MciResponse<?> targetResponse, TransactionDefinition definition);
}
