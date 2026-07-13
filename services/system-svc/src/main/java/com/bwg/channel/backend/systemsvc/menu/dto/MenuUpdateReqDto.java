package com.bwg.channel.backend.systemsvc.menu.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/** 메뉴 수정 요청 모델 */
@Alias("MenuUpdateReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "MenuUpdate", endpoints = {"update"})
public class MenuUpdateReqDto {

    @ApiField(description = "상위 메뉴 ID", optional = {"update"})
    private Long parentMenuId;

    @ApiField(description = "메뉴명", example = "대시보드", required = {"update"})
    private String menuNm;

    @ApiField(description = "메뉴 유형", example = "MENU", optional = {"update"})
    private String menuType;

    @ApiField(description = "화면 경로", example = "/dashboard", optional = {"update"})
    private String path;

    @ApiField(description = "프론트엔드 컴포넌트 경로", example = "DashboardView", optional = {"update"})
    private String component;

    @ApiField(description = "아이콘명", example = "LayoutDashboard", optional = {"update"})
    private String icon;

    @ApiField(description = "메뉴 깊이", example = "1", optional = {"update"})
    private Integer depth;

    @ApiField(description = "정렬 순서", example = "1", optional = {"update"})
    private Integer sortSeq;

    @ApiField(description = "노출 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"update"})
    private String visibleYn;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"update"})
    private String useYn;

    @ApiField(description = "비고", optional = {"update"})
    private String remark;

    @ApiField(description = "요청자 ID", example = "admin", optional = {"update"})
    private String createdBy;
}
