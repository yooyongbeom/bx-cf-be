package com.bwg.channel.backend.systemsvc.referencedata.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.OffsetDateTime;

/** 기준정보 최신 버전 조회 응답 모델 */
@Alias("ReferenceDataVersionResDto")
@Data
@ApiDto(type = ApiType.RESPONSE, name = "ReferenceDataVersion", endpoints = {"latest"})
public class ReferenceDataVersionResDto {

    @ApiField(description = "기준정보 버전 ID", example = "1", optional = {"latest"})
    private Long versionId;

    @ApiField(description = "기준정보 유형", example = "MENU", optional = {"latest"})
    private String refType;

    @ApiField(description = "버전 번호", example = "0.0.1", optional = {"latest"})
    private String versionNo;

    @ApiField(description = "최종 변경 일시", example = "2026-06-25T16:04:14+09:00", optional = {"latest"})
    private OffsetDateTime lastChangedAt;

    @ApiField(description = "최종 변경자 ID", example = "system", optional = {"latest"})
    private String lastChangedBy;

    @ApiField(description = "비고", example = "메뉴 샘플 데이터 최초 버전", optional = {"latest"})
    private String remark;
}
