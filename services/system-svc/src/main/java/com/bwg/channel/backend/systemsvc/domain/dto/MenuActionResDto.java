package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;

/** 메뉴 기능 목록 응답 모델 */
@Data
@ApiDto(type = ApiType.RESPONSE, name = "MenuAction", endpoints = {"list"})
public class MenuActionResDto {

    @ApiField(description = "메뉴 기능 ID", optional = {"list"})
    private Long actionId;

    @ApiField(description = "메뉴 ID", optional = {"list"})
    private Long menuId;

    @ApiField(description = "기능 코드", example = "READ", optional = {"list"})
    private String actionCd;

    @ApiField(description = "기능명", example = "조회", optional = {"list"})
    private String actionNm;

    @ApiField(description = "HTTP 메서드", example = "GET", optional = {"list"})
    private String httpMethod;

    @ApiField(description = "API 패턴", example = "/channel/backend/api/v1/system/menus/**", optional = {"list"})
    private String apiPattern;

    @ApiField(description = "정렬 순서", example = "1", optional = {"list"})
    private Integer sortSeq;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"list"})
    private String useYn;

    @ApiField(description = "생성자 ID", optional = {"list"})
    private String createdBy;

    @ApiField(description = "수정자 ID", optional = {"list"})
    private String updatedBy;
}
