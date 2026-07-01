package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import com.bwg.channel.backend.typebridge.annotation.ApiType;
import lombok.Data;

/** 메뉴 목록 응답 모델 (전체 메뉴 / 역할별 메뉴 공통) */
@Data
@ApiDto(type = ApiType.RESPONSE, name = "Menu", endpoints = {"list"})
public class MenuResDto {

    @ApiField(description = "메뉴 ID", optional = {"list"})
    private Long menuId;

    @ApiField(description = "상위 메뉴 ID", optional = {"list"})
    private Long parentMenuId;

    @ApiField(description = "메뉴 코드", example = "DASHBOARD", optional = {"list"})
    private String menuCd;

    @ApiField(description = "메뉴명", example = "대시보드", optional = {"list"})
    private String menuNm;

    @ApiField(description = "메뉴 유형", example = "MENU", optional = {"list"})
    private String menuType;

    @ApiField(description = "화면 경로", example = "/dashboard", optional = {"list"})
    private String path;

    @ApiField(description = "프론트엔드 컴포넌트 경로", example = "DashboardView", optional = {"list"})
    private String component;

    @ApiField(description = "아이콘명", example = "LayoutDashboard", optional = {"list"})
    private String icon;

    @ApiField(description = "메뉴 깊이", example = "1", optional = {"list"})
    private Integer depth;

    @ApiField(description = "정렬 순서", example = "1", optional = {"list"})
    private Integer sortSeq;

    @ApiField(description = "노출 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"list"})
    private String visibleYn;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"list"})
    private String useYn;

    @ApiField(description = "비고", optional = {"list"})
    private String remark;

    @ApiField(description = "생성자 ID", example = "admin", optional = {"list"})
    private String createdBy;

    @ApiField(description = "수정자 ID", example = "admin", optional = {"list"})
    private String updatedBy;
}
