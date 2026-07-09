package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * API 응답 표준 모델.
 * <p>
 * {@link CommonResponse}의 success/code/msg/payload에 요청 추적 ID와 목록 페이징 메타데이터를 덧붙인다.
 */
@Getter
@Setter
public class ApiResponse<T> extends CommonResponse<T> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String requestId;  // API 요청 추적용 ID

    // 목록 응답에서만 내려가는 페이지 메타데이터. 단건 응답에서는 JSON에 포함하지 않는다.
    @ApiField(description = "페이지 결과 정보", optional = {"list"})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PaginationResDto pagination;

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

    // 성공 응답 생성 helper (목록 메타데이터 포함)
    public static <T> ApiResponse<T> success(T data, PaginationResDto pagination) {
        ApiResponse<T> response = success(data);
        // payload 생성 후 pagination만 추가해 success 응답 생성 규칙을 한 곳으로 모은다.
        response.setPagination(pagination);
        return response;
    }
}
