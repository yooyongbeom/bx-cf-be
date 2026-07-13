package com.bwg.channel.backend.mcisvc.sample;

import com.bwg.channel.backend.mcicommon.domain.MciRequest;
import com.bwg.channel.backend.mcicommon.domain.MciResponse;
import com.bwg.channel.backend.mcicommon.domain.MciResult;
import com.bwg.channel.backend.mcicommon.spi.MciAdapter;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 샘플 계정계 adapter.
 * <p>
 * 실제 고객사에서는 이 위치에 TCP, REST, SOAP, MQ adapter를 구현한다. 샘플은 통신 없이 요청 데이터를 그대로
 * 되돌려 registry/mapper/router 연결을 검증하는 용도다.
 */
@Component
public class SampleCoreBankingAdapter implements MciAdapter {

    /** YAML transaction.adapter와 일치해야 하는 adapter 이름. */
    private static final String ADAPTER_NAME = "sampleCoreBankingAdapter";

    @Override
    public String adapterName() {
        return ADAPTER_NAME;
    }

    /** 샘플 응답을 만들어 실제 adapter 교체 전에도 end-to-end 흐름을 확인할 수 있게 한다. */
    @Override
    public MciResponse<?> call(MciRequest<?> request) {
        return new MciResponse<>(
                request.getHeader(),
                MciResult.success(),
                Map.of(
                        "adapter", ADAPTER_NAME,
                        "targetSystem", "CORE_BANKING",
                        "echo", request.getData()
                )
        );
    }
}
