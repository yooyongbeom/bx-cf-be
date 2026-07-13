package com.bwg.channel.backend.mcicommon.router;

import com.bwg.channel.backend.mcicommon.domain.MciHeader;
import com.bwg.channel.backend.mcicommon.domain.MciRequest;
import com.bwg.channel.backend.mcicommon.domain.MciResponse;
import com.bwg.channel.backend.mcicommon.domain.MciResult;
import com.bwg.channel.backend.mcicommon.registry.TransactionDefinition;
import com.bwg.channel.backend.mcicommon.registry.TransactionRegistry;
import com.bwg.channel.backend.mcicommon.spi.MciAdapter;
import com.bwg.channel.backend.mcicommon.spi.MciMapper;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MciRouterTest {

    @Test
    void routesStandardRequestToConfiguredMapperAndAdapter() {
        TransactionDefinition definition = new TransactionDefinition();
        definition.setCode("CIF001");
        definition.setName("고객 기본정보 조회");
        definition.setEnabled(true);
        definition.setChannels(List.of("WEB"));
        definition.setAdapter("sampleAdapter");
        definition.setRequestMapper("passThroughMapper");
        definition.setResponseMapper("passThroughMapper");
        definition.setTimeoutMs(3000);

        MciRouter router = new MciRouter(
                code -> Optional.of(definition),
                List.of(new SampleAdapter()),
                List.of(new PassThroughMapper())
        );

        MciHeader header = new MciHeader();
        header.setTraceId("trace-1");
        header.setChannelCode("WEB");
        header.setTransactionCode("CIF001");
        header.setUserId("hong");
        header.setRequestAt(OffsetDateTime.parse("2026-07-13T15:30:00+09:00"));

        MciResponse<Map<String, Object>> response = router.execute(new MciRequest<>(header, Map.of("customerId", "C001")));

        assertThat(response.getHeader().getTraceId()).isEqualTo("trace-1");
        assertThat(response.getHeader().getTransactionCode()).isEqualTo("CIF001");
        assertThat(response.getResult().isSuccess()).isTrue();
        assertThat(response.getData()).containsEntry("adapter", "sampleAdapter");
        assertThat(response.getData()).containsEntry("customerId", "C001");
    }

    private static class SampleAdapter implements MciAdapter {
        @Override
        public String adapterName() {
            return "sampleAdapter";
        }

        @Override
        public MciResponse<?> call(MciRequest<?> request) {
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = (Map<String, Object>) request.getData();
            return new MciResponse<>(
                    request.getHeader(),
                    MciResult.success(),
                    Map.of(
                            "adapter", adapterName(),
                            "customerId", requestData.get("customerId")
                    )
            );
        }
    }

    private static class PassThroughMapper implements MciMapper {
        @Override
        public String mapperName() {
            return "passThroughMapper";
        }

        @Override
        public MciRequest<?> toTargetRequest(MciRequest<?> standardRequest, TransactionDefinition definition) {
            return standardRequest;
        }

        @Override
        public MciResponse<?> toStandardResponse(MciResponse<?> targetResponse, TransactionDefinition definition) {
            return targetResponse;
        }
    }
}
