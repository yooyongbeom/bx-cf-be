package com.bwg.channel.backend.authcore.support;

import com.bwg.channel.backend.common.constants.enums.BwgErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public final class WebFluxAuthErrorResponseWriter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final byte[] FALLBACK_ERROR_RESPONSE =
            "{\"success\":false,\"code\":\"-9999\",\"msg\":\"Error response serialization failed\",\"payload\":null}"
                    .getBytes(StandardCharsets.UTF_8);

    private WebFluxAuthErrorResponseWriter() {
    }

    public static Mono<Void> write(
            ServerWebExchange exchange,
            HttpStatus status,
            BwgErrorCode errorCode
    ) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBuffer buffer = response.bufferFactory().wrap(toResponseBytes(errorCode));
        return response.writeWith(Mono.just(buffer));
    }

    private static byte[] toResponseBytes(BwgErrorCode errorCode) {
        try {
            ApiResponse<?> apiResponse = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());
            return OBJECT_MAPPER.writeValueAsBytes(apiResponse);
        } catch (JsonProcessingException e) {
            return FALLBACK_ERROR_RESPONSE;
        }
    }
}
