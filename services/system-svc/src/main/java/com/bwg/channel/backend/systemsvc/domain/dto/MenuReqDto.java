package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.common.domain.dto.BaseAuditReqDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 메뉴 등록/수정 요청 모델 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiDto(type = ApiType.REQUEST, name = "Menu", endpoints = {"create", "update"})
public class MenuReqDto extends BaseAuditReqDto {

    // 경로변수(menuId)로 주입되는 값. 요청 스키마에는 노출하지 않는다(hidden).
    @ApiField(description = "메뉴 ID", hidden = true)
    private Long menuId;

    @ApiField(description = "상위 메뉴 ID", optional = {"create", "update"})
    private Long parentMenuId;

    @ApiField(description = "메뉴 코드", example = "DASHBOARD", required = {"create"})
    private String menuCd;

    @ApiField(description = "메뉴명", example = "대시보드", required = {"create", "update"})
    private String menuNm;

    @ApiField(description = "메뉴 유형", example = "MENU", optional = {"create", "update"})
    private String menuType;

    @ApiField(description = "화면 경로", example = "/dashboard", optional = {"create", "update"})
    private String path;

    @ApiField(description = "프론트엔드 컴포넌트 경로", example = "DashboardView", optional = {"create", "update"})
    private String component;

    @ApiField(description = "아이콘명", example = "LayoutDashboard", optional = {"create", "update"})
    private String icon;

    @ApiField(description = "메뉴 깊이", example = "1", optional = {"create", "update"})
    private Integer depth;

    @ApiField(description = "정렬 순서", example = "1", optional = {"create", "update"})
    private Integer sortSeq;

    @ApiField(description = "노출 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"create", "update"})
    private String visibleYn;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"create", "update"})
    private String useYn;

    @ApiField(description = "비고", optional = {"create", "update"})
    private String remark;

}
