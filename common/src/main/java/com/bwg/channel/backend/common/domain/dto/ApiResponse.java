package com.bwg.channel.backend.common.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> extends CommonResponse<T> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String requestId;  // API 요청 추적용 ID

    public ApiResponse(boolean success, String code, String msg, T payload) {
        super(success, code, msg, payload);
    }

    // 실패 응답 생성 helper
    public static <T> ApiResponse<T> fail(String code, String msg) {
        return new ApiResponse<>(false, code, msg, null);
    }

    // 성공 응답 생성 helper
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "0", "success", data);
    }
}
