package com.bwg.channel.backend.systemsvc.commoncode.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** 공통코드 목록/상세 응답 모델 */
@Alias("CommonCodeResDto")
@Data
@ApiDto(type = ApiType.RESPONSE, name = "CommonCode", endpoints = {"list", "detail"})
public class CommonCodeResDto {

    @ApiField(description = "공통코드 ID", optional = {"list", "detail"})
    private Long codeId;

    @ApiField(description = "공통코드 그룹 ID", optional = {"list", "detail"})
    private Long groupId;

    @ApiField(description = "공통코드 그룹 코드", optional = {"list", "detail"})
    private String groupCd;

    @ApiField(description = "공통코드", example = "Y", optional = {"list", "detail"})
    private String code;

    @ApiField(description = "공통코드명", example = "사용", optional = {"list", "detail"})
    private String codeNm;

    @ApiField(description = "공통코드 설명", optional = {"list", "detail"})
    private String codeDesc;

    @ApiField(description = "상위 공통코드 ID", optional = {"list"})
    private Long parentCodeId;

    @ApiField(description = "정렬 순서", example = "1", optional = {"list", "detail"})
    private Integer sortSeq;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"list", "detail"})
    private String useYn;

    @ApiField(description = "유효 시작일", example = "2026-01-01", optional = {"list"})
    private LocalDate validFrom;

    @ApiField(description = "유효 종료일", example = "2026-12-31", optional = {"list"})
    private LocalDate validTo;

    @ApiField(description = "추가 데이터 JSON 문자열", example = "{}", optional = {"list"})
    private String extraData;

    @ApiField(description = "생성자 ID", example = "admin", optional = {"list", "detail"})
    private String createdBy;

    @ApiField(description = "수정자 ID", example = "admin", optional = {"list", "detail"})
    private String updatedBy;

    @ApiField(description = "생성 일시", example = "2026-01-01T09:00:00+09:00", optional = {"list", "detail"})
    private OffsetDateTime createdAt;

    @ApiField(description = "수정 일시", example = "2026-01-01T10:00:00+09:00", optional = {"list", "detail"})
    private OffsetDateTime updatedAt;
}
