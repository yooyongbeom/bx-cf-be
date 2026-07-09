package com.bwg.channel.backend.systemsvc.menu.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.OffsetDateTime;

/** 메뉴 목록 응답 모델 */
@Alias("MenuResDto")
@Data
@ApiDto(type = ApiType.RESPONSE, name = "Menu", endpoints = {"list", "detail"})
public class MenuResDto {

    @ApiField(description = "메뉴 ID", optional = {"list", "detail"})
    private Long menuId;

    @ApiField(description = "상위 메뉴 ID", optional = {"list", "detail"})
    private Long parentMenuId;

    @ApiField(description = "메뉴 코드", example = "DASHBOARD", optional = {"list", "detail"})
    private String menuCd;

    @ApiField(description = "메뉴명", example = "대시보드", optional = {"list", "detail"})
    private String menuNm;

    @ApiField(description = "메뉴 유형", example = "MENU", optional = {"list", "detail"})
    private String menuType;

    @ApiField(description = "화면 경로", example = "/dashboard", optional = {"list", "detail"})
    private String path;

    @ApiField(description = "프론트엔드 컴포넌트 경로", example = "DashboardView", optional = {"list", "detail"})
    private String component;

    @ApiField(description = "아이콘명", example = "LayoutDashboard", optional = {"list", "detail"})
    private String icon;

    @ApiField(description = "메뉴 깊이", example = "1", optional = {"list", "detail"})
    private Integer depth;

    @ApiField(description = "정렬 순서", example = "1", optional = {"list", "detail"})
    private Integer sortSeq;

    @ApiField(description = "노출 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"list", "detail"})
    private String visibleYn;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"list", "detail"})
    private String useYn;

    @ApiField(description = "비고", optional = {"list", "detail"})
    private String remark;

    @ApiField(description = "생성자 ID", example = "admin", optional = {"list", "detail"})
    private String createdBy;

    @ApiField(description = "수정자 ID", example = "admin", optional = {"list", "detail"})
    private String updatedBy;

    @ApiField(description = "생성 일시", example = "2026-01-01T09:00:00+09:00", optional = {"list", "detail"})
    private OffsetDateTime createdAt;

    @ApiField(description = "수정 일시", example = "2026-01-01T10:00:00+09:00", optional = {"list", "detail"})
    private OffsetDateTime updatedAt;
}
