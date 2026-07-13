package com.bwg.channel.backend.systemsvc.menu.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/** 메뉴 등록 요청 모델 */
@Alias("MenuCreateReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "Menu", endpoints = {"create"})
public class MenuCreateReqDto {

    @ApiField(description = "상위 메뉴 ID", optional = {"create"})
    private Long parentMenuId;

    @ApiField(description = "메뉴 코드", example = "DASHBOARD", required = {"create"})
    private String menuCd;

    @ApiField(description = "메뉴명", example = "대시보드", required = {"create"})
    private String menuNm;

    @ApiField(description = "메뉴 유형", example = "MENU", optional = {"create"})
    private String menuType;

    @ApiField(description = "화면 경로", example = "/dashboard", optional = {"create"})
    private String path;

    @ApiField(description = "프론트엔드 컴포넌트 경로", example = "DashboardView", optional = {"create"})
    private String component;

    @ApiField(description = "아이콘명", example = "LayoutDashboard", optional = {"create"})
    private String icon;

    @ApiField(description = "메뉴 깊이", example = "1", optional = {"create"})
    private Integer depth;

    @ApiField(description = "정렬 순서", example = "1", optional = {"create"})
    private Integer sortSeq;

    @ApiField(description = "노출 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"create"})
    private String visibleYn;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"create"})
    private String useYn;

    @ApiField(description = "비고", optional = {"create"})
    private String remark;

}
