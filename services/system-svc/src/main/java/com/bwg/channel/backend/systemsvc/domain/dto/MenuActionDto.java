package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import lombok.Data;

/**
 * 메뉴에서 허용하는 기능과 API 패턴 정보를 담는 DTO.
 */
@Data
@ApiDto(name = "MenuAction", endpoints = {})
public class MenuActionDto {
    @ApiField(description = "메뉴 기능 ID")
    private Long actionId;

    @ApiField(description = "메뉴 ID")
    private Long menuId;

    @ApiField(description = "기능 코드", example = "READ")
    private String actionCd;

    @ApiField(description = "기능명", example = "조회")
    private String actionNm;

    @ApiField(description = "HTTP 메서드", example = "GET")
    private String httpMethod;

    @ApiField(description = "API 패턴", example = "/channel/backend/api/v1/system/menus/**")
    private String apiPattern;

    @ApiField(description = "정렬 순서", example = "1")
    private Integer sortSeq;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"})
    private String useYn;

    @ApiField(description = "생성자 ID")
    private String createdBy;

    @ApiField(description = "수정자 ID")
    private String updatedBy;
}
