package com.bwg.channel.backend.systemsvc.menu.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.OffsetDateTime;

/** 메뉴 상세 응답 모델 */
@Alias("MenuDetailResDto")
@Data
@ApiDto(type = ApiType.RESPONSE, name = "Menu", endpoints = {"detail"})
public class MenuDetailResDto {

    @ApiField(description = "메뉴 ID", optional = {"detail"})
    private Long menuId;

    @ApiField(description = "상위 메뉴 ID", optional = {"detail"})
    private Long parentMenuId;

    @ApiField(description = "메뉴 코드", example = "DASHBOARD", optional = {"detail"})
    private String menuCd;

    @ApiField(description = "메뉴명", example = "대시보드", optional = {"detail"})
    private String menuNm;

    @ApiField(description = "메뉴 유형", example = "MENU", optional = {"detail"})
    private String menuType;

    @ApiField(description = "화면 경로", example = "/dashboard", optional = {"detail"})
    private String path;

    @ApiField(description = "프론트엔드 컴포넌트 경로", example = "DashboardView", optional = {"detail"})
    private String component;

    @ApiField(description = "아이콘명", example = "LayoutDashboard", optional = {"detail"})
    private String icon;

    @ApiField(description = "메뉴 깊이", example = "1", optional = {"detail"})
    private Integer depth;

    @ApiField(description = "정렬 순서", example = "1", optional = {"detail"})
    private Integer sortSeq;

    @ApiField(description = "노출 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"detail"})
    private String visibleYn;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"detail"})
    private String useYn;

    @ApiField(description = "비고", optional = {"detail"})
    private String remark;

    @ApiField(description = "생성자 ID", example = "admin", optional = {"detail"})
    private String createdBy;

    @ApiField(description = "수정자 ID", example = "admin", optional = {"detail"})
    private String updatedBy;

    @ApiField(description = "생성 일시", example = "2026-01-01T09:00:00+09:00", optional = {"detail"})
    private OffsetDateTime createdAt;

    @ApiField(description = "수정 일시", example = "2026-01-01T10:00:00+09:00", optional = {"detail"})
    private OffsetDateTime updatedAt;
}
