package com.bwg.channel.backend.systemsvc.menu.service;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.RoleMenuSaveReqDto;

import java.util.List;

/**
 * 메뉴와 역할별 메뉴 권한 관리 기능의 서비스 계약.
 */
public interface MenuService {

    /**
     * 전체 메뉴 목록 조회
     */
    ApiResponse<List<MenuResDto>> getMenus();

    /**
     * 메뉴 ID 기준 상세 조회
     */
    ApiResponse<MenuResDto> getMenu(Long menuId);

    /**
     * 메뉴 등록
     */
    ApiResponse<Void> createMenu(ApiRequest<MenuReqDto> paramDto);

    /**
     * 메뉴 수정
     */
    ApiResponse<Void> updateMenu(Long menuId, ApiRequest<MenuReqDto> paramDto);

    /**
     * 메뉴 ID 기준 기능 목록 조회
     */
    ApiResponse<List<MenuActionResDto>> getMenuActions(Long menuId);

    /**
     * 역할 ID 기준 메뉴 목록 조회
     */
    ApiResponse<List<MenuResDto>> getMenusByRoleId(Long roleId);

    /**
     * 역할별 메뉴 권한 저장
     */
    ApiResponse<Void> saveRoleMenus(Long roleId, ApiRequest<RoleMenuSaveReqDto> paramDto);
}
