package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import lombok.Data;

/**
 * 메뉴의 계층, 화면 경로, 노출 속성을 담는 DTO.
 */
@Data
@ApiDto(name = "Menu", endpoints = {"create", "update"})
public class MenuDto {
    @ApiField(description = "메뉴 ID")
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

    @ApiField(description = "생성자 ID", example = "admin", optional = {"create"})
    private String createdBy;

    @ApiField(description = "수정자 ID", example = "admin", optional = {"update"})
    private String updatedBy;
}
