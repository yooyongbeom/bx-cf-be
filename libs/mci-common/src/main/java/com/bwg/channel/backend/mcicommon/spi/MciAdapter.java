package com.bwg.channel.backend.mcicommon.spi;

import com.bwg.channel.backend.mcicommon.domain.MciRequest;
import com.bwg.channel.backend.mcicommon.domain.MciResponse;

/**
 * 고객사 시스템 통신 adapter SPI.
 * <p>
 * TCP, REST, SOAP, MQ 같은 프로토콜 차이는 adapter 구현체로 분리한다. product core/common은 프로토콜을 알지 않는다.
 */
public interface MciAdapter {

    /** transaction registry에서 참조하는 adapter 이름. */
    String adapterName();

    /** mapper가 만든 대상 시스템 요청을 호출하고 대상 시스템 응답을 반환한다. */
    MciResponse<?> call(MciRequest<?> request);
}
