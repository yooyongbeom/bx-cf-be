package com.bwg.channel.backend.mcicommon.router;

import com.bwg.channel.backend.mcicommon.constants.MciErrorCode;
import com.bwg.channel.backend.mcicommon.domain.MciHeader;
import com.bwg.channel.backend.mcicommon.domain.MciRequest;
import com.bwg.channel.backend.mcicommon.domain.MciResponse;
import com.bwg.channel.backend.mcicommon.domain.MciResult;
import com.bwg.channel.backend.mcicommon.exception.MciException;
import com.bwg.channel.backend.mcicommon.registry.TransactionDefinition;
import com.bwg.channel.backend.mcicommon.registry.TransactionRegistry;
import com.bwg.channel.backend.mcicommon.spi.MciAdapter;
import com.bwg.channel.backend.mcicommon.spi.MciMapper;
import org.junit.jupiter.api.Test;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void rejectsMissingRequestHeaderWithTypedCode() {
        assertMciCode(() -> emptyRouter().execute(null), MciErrorCode.REQUEST_HEADER_REQUIRED);
    }

    @Test
    void rejectsMissingTransactionCodeWithTypedCode() {
        assertMciCode(
                () -> emptyRouter().execute(request(" ", "WEB")),
                MciErrorCode.TRANSACTION_CODE_REQUIRED
        );
    }

    @Test
    void rejectsUnknownTransactionWithTypedCode() {
        assertMciCode(
                () -> emptyRouter().execute(request("UNKNOWN", "WEB")),
                MciErrorCode.TRANSACTION_NOT_REGISTERED
        );
    }

    @Test
    void rejectsDisabledTransactionWithTypedCode() {
        TransactionDefinition definition = definition(false);

        assertMciCode(
                () -> router(definition, List.of(), List.of()).execute(request("CIF001", "WEB")),
                MciErrorCode.TRANSACTION_DISABLED
        );
    }

    @Test
    void rejectsDisallowedChannelWithTypedCode() {
        TransactionDefinition definition = definition(true);

        assertMciCode(
                () -> router(definition, List.of(), List.of()).execute(request("CIF001", "BATCH")),
                MciErrorCode.CHANNEL_NOT_ALLOWED
        );
    }

    @Test
    void rejectsMissingAdapterWithTypedCode() {
        TransactionDefinition definition = definition(true);

        assertMciCode(
                () -> router(definition, List.of(), List.of(new PassThroughMapper()))
                        .execute(request("CIF001", "WEB")),
                MciErrorCode.ADAPTER_NOT_REGISTERED
        );
    }

    @Test
    void rejectsMissingMapperWithTypedCode() {
        TransactionDefinition definition = definition(true);

        assertMciCode(
                () -> router(definition, List.of(new SampleAdapter()), List.of())
                        .execute(request("CIF001", "WEB")),
                MciErrorCode.MAPPER_NOT_REGISTERED
        );
    }

    private MciRouter emptyRouter() {
        return new MciRouter(code -> Optional.empty(), List.of(), List.of());
    }

    private MciRouter router(
            TransactionDefinition definition,
            List<MciAdapter> adapters,
            List<MciMapper> mappers
    ) {
        return new MciRouter(code -> Optional.of(definition), adapters, mappers);
    }

    private TransactionDefinition definition(boolean enabled) {
        TransactionDefinition definition = new TransactionDefinition();
        definition.setCode("CIF001");
        definition.setName("customer inquiry");
        definition.setEnabled(enabled);
        definition.setChannels(List.of("WEB"));
        definition.setAdapter("sampleAdapter");
        definition.setRequestMapper("passThroughMapper");
        definition.setResponseMapper("passThroughMapper");
        definition.setTimeoutMs(3000);
        return definition;
    }

    private MciRequest<Map<String, Object>> request(String transactionCode, String channelCode) {
        MciHeader header = new MciHeader();
        header.setTransactionCode(transactionCode);
        header.setChannelCode(channelCode);
        return new MciRequest<>(header, Map.of());
    }

    private void assertMciCode(ThrowingCallable invocation, MciErrorCode expectedCode) {
        assertThatThrownBy(invocation)
                .isInstanceOf(MciException.class)
                .satisfies(error -> assertThat(((MciException) error).getCode()).isEqualTo(expectedCode));
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
