package com.bwg.channel.backend.common.domain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * null payload를 빈 객체({})로 표현하고 싶을 때 사용하는 Jackson serializer
 * <p>
 * 현재 공통 응답에서는 null을 그대로 노출하도록 비활성화되어 있지만,
 * 서비스별 응답 정책이 바뀔 때 {@code @JsonSerialize(nullsUsing = EmptyObjectSerializer.class)}로 재사용할 수 있다.
 */
public class EmptyObjectSerializer extends JsonSerializer<Object> {
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // 값의 실제 타입과 무관하게 JSON object 시작/종료만 기록해 {}를 만든다.
        gen.writeStartObject();
        gen.writeEndObject();
    }
}
