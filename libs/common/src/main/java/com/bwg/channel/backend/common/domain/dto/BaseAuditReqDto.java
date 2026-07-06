package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import lombok.Data;

/**
 * 등록/수정 요청 DTO에서 공통으로 사용하는 등록/수정 정보 입력 필드 모델.
 */
@Data
public class BaseAuditReqDto {

    @ApiField(description = "생성자 ID", example = "admin", optional = {"create", "save"})
    private String createdBy;

    @ApiField(description = "수정자 ID", example = "admin", optional = {"update"})
    private String updatedBy;
}
