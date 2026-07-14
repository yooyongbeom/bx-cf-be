package com.bwg.channel.backend.mcicommon.router;

import com.bwg.channel.backend.mcicommon.constants.MciErrorCode;
import com.bwg.channel.backend.mcicommon.domain.MciHeader;
import com.bwg.channel.backend.mcicommon.domain.MciRequest;
import com.bwg.channel.backend.mcicommon.domain.MciResponse;
import com.bwg.channel.backend.mcicommon.exception.MciException;
import com.bwg.channel.backend.mcicommon.registry.TransactionDefinition;
import com.bwg.channel.backend.mcicommon.registry.TransactionRegistry;
import com.bwg.channel.backend.mcicommon.spi.MciAdapter;
import com.bwg.channel.backend.mcicommon.spi.MciMapper;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MCI 표준 거래 router.
 * <p>
 * router는 거래 코드로 registry를 조회하고, registry에 지정된 request mapper -> adapter -> response mapper 순서로
 * 거래를 실행한다. 고객사별 구현은 SPI로 주입되므로 router 자체는 금융권 공통 기능으로 유지된다.
 */
public class MciRouter {

    /** 거래 코드별 adapter/mapper 선택 정보를 제공하는 registry. */
    private final TransactionRegistry transactionRegistry;

    /** adapter 이름으로 빠르게 찾기 위한 map. */
    private final Map<String, MciAdapter> adapters;

    /** mapper 이름으로 빠르게 찾기 위한 map. */
    private final Map<String, MciMapper> mappers;

    public MciRouter(TransactionRegistry transactionRegistry, List<MciAdapter> adapters, List<MciMapper> mappers) {
        this.transactionRegistry = transactionRegistry;
        this.adapters = adapters.stream().collect(Collectors.toMap(MciAdapter::adapterName, Function.identity()));
        this.mappers = mappers.stream().collect(Collectors.toMap(MciMapper::mapperName, Function.identity()));
    }

    /**
     * 표준 요청을 실행하고 표준 응답으로 반환한다.
     *
     * @param request 채널/backend 표준 요청
     * @param <T> 호출자가 기대하는 응답 payload 타입
     * @return 표준 응답
     */
    @SuppressWarnings("unchecked")
    public <T> MciResponse<T> execute(MciRequest<?> request) {
        long startTime = System.currentTimeMillis();

        // 표준 헤더가 없으면 거래 코드, 채널 코드, 추적 ID를 확인할 수 없으므로 가장 먼저 검증한다.
        MciHeader header = validateAndGetHeader(request);

        // 거래 정의는 "어떤 adapter/mapper를 쓸지"를 결정하는 기준 데이터이므로 router 실행의 중심이다.
        TransactionDefinition definition = findEnabledDefinition(header);

        // 거래별 허용 채널을 registry에 두어 같은 거래라도 WEB/MOBILE/BATCH 노출 여부를 분리한다.
        validateChannel(header, definition);

        // 고객사별 차이는 mapper와 adapter 구현체에만 두고, router는 이름 기반으로 조립만 수행한다.
        MciMapper requestMapper = findMapper(definition.getRequestMapper(), "requestMapper");
        MciAdapter adapter = findAdapter(definition.getAdapter());
        MciMapper responseMapper = findMapper(definition.getResponseMapper(), "responseMapper");

        // 표준 요청 -> 고객사 요청 -> 고객사 응답 -> 표준 응답 순서로 변환해 채널은 항상 표준 모델만 보게 한다.
        MciRequest<?> targetRequest = requestMapper.toTargetRequest(request, definition);
        MciResponse<?> targetResponse = adapter.call(targetRequest);
        MciResponse<?> standardResponse = responseMapper.toStandardResponse(targetResponse, definition);

        enrichResponseHeader(standardResponse, header, startTime);
        return (MciResponse<T>) standardResponse;
    }

    private MciHeader validateAndGetHeader(MciRequest<?> request) {
        if (request == null || request.getHeader() == null) {
            throw MciException.of(MciErrorCode.REQUEST_HEADER_REQUIRED);
        }
        MciHeader header = request.getHeader();
        if (!hasText(header.getTransactionCode())) {
            throw MciException.of(MciErrorCode.TRANSACTION_CODE_REQUIRED);
        }
        return header;
    }

    private TransactionDefinition findEnabledDefinition(MciHeader header) {
        // 비활성 거래도 설정에는 남겨둘 수 있지만, 실행 시점에는 명시적으로 차단한다.
        TransactionDefinition definition = transactionRegistry.findByCode(header.getTransactionCode())
                .orElseThrow(() -> MciException.of(
                        MciErrorCode.TRANSACTION_NOT_REGISTERED,
                        Collections.singletonMap("transactionCode", header.getTransactionCode())
                ));
        if (!definition.isEnabled()) {
            throw MciException.of(
                    MciErrorCode.TRANSACTION_DISABLED,
                    Collections.singletonMap("transactionCode", header.getTransactionCode())
            );
        }
        return definition;
    }

    private void validateChannel(MciHeader header, TransactionDefinition definition) {
        // channels가 비어 있으면 아직 채널 제한을 두지 않은 공통 거래로 본다.
        if (definition.getChannels() == null || definition.getChannels().isEmpty()) {
            return;
        }
        if (!definition.getChannels().contains(header.getChannelCode())) {
            throw MciException.of(
                    MciErrorCode.CHANNEL_NOT_ALLOWED,
                    Collections.singletonMap("channelCode", header.getChannelCode())
            );
        }
    }

    private MciAdapter findAdapter(String adapterName) {
        if (!hasText(adapterName) || !adapters.containsKey(adapterName)) {
            throw MciException.of(
                    MciErrorCode.ADAPTER_NOT_REGISTERED,
                    Collections.singletonMap("adapterName", adapterName)
            );
        }
        return adapters.get(adapterName);
    }

    private MciMapper findMapper(String mapperName, String role) {
        if (!hasText(mapperName) || !mappers.containsKey(mapperName)) {
            throw MciException.of(
                    MciErrorCode.MAPPER_NOT_REGISTERED,
                    Map.of("mapperName", String.valueOf(mapperName), "role", role)
            );
        }
        return mappers.get(mapperName);
    }

    private void enrichResponseHeader(MciResponse<?> response, MciHeader fallbackHeader, long startTime) {
        // adapter가 헤더를 누락해도 traceId가 끊기지 않도록 요청 헤더를 fallback으로 사용한다.
        if (response.getHeader() == null) {
            response.setHeader(fallbackHeader);
        }
        response.getHeader().setResponseAt(OffsetDateTime.now());
        response.getHeader().setElapsedMs(System.currentTimeMillis() - startTime);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
