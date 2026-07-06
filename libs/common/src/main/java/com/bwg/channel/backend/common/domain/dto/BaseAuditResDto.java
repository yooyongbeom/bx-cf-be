package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 조회 응답 DTO에서 공통으로 사용하는 등록/수정 정보 출력 필드 모델.
 */
@Data
public class BaseAuditResDto {

    @ApiField(description = "생성자 ID", example = "admin", optional = {"list", "detail"})
    private String createdBy;

    @ApiField(description = "수정자 ID", example = "admin", optional = {"list", "detail"})
    private String updatedBy;

    @ApiField(description = "생성 일시", example = "2026-01-01T09:00:00", optional = {"list", "detail"})
    private LocalDateTime createdAt;

    @ApiField(description = "수정 일시", example = "2026-01-01T10:00:00", optional = {"list", "detail"})
    private LocalDateTime updatedAt;
}
