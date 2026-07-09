package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.domain.EmptyObjectSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

/**
 * 모든 API 응답이 공유하는 최소 응답 구조
 * <p>
 * 성공 여부, 업무 코드, 메시지, payload만 포함하고 추적 ID/페이징 같은 확장은 {@link ApiResponse}에서 담당한다.
 */
@Getter
@Setter
public class CommonResponse<T> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean success;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String msg;

    // 일단은 null이면 null 표시되게 가자
    // payload가 null일 때 {}로 내려야 하는 정책이 생기면 아래 serializer를 다시 활성화한다.
    //@JsonSerialize(nullsUsing = EmptyObjectSerializer.class)
    private T payload;

    public CommonResponse(boolean success, String code, String msg, T payload)
    {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.payload = payload;
    }

    // 정적 팩토리 메서드
    public static <T> CommonResponse<T> of(boolean success, String code, String msg, T payload) {
        return new CommonResponse<>(success, code, msg, payload);
    }

    // 성공 응답 (payload 포함)
    public static <T> CommonResponse<T> success(T payload) {
        return new CommonResponse<>(true, "0", "SUCCESS!!!!", payload);
    }

    // 성공 응답 (payload 없는 경우)
    public static <T> CommonResponse<T> success() {
        return new CommonResponse<>(true, "0", "SUCCESS!!!!", null);
    }

    // 실패 응답 (코드와 메시지 지정)
    public static <T> CommonResponse<T> failure(String code, String msg) {
        return new CommonResponse<>(false, code, msg, null);
    }
}
