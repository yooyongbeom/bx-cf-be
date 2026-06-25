package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuActionDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuDto;
import com.bwg.channel.backend.systemsvc.domain.dto.RoleMenuSaveDto;

import java.util.List;

/**
 * 메뉴와 역할별 메뉴 권한 관리 기능의 서비스 계약.
 */
public interface MenuService {

    /**
     * 전체 메뉴 목록을 조회한다.
     */
    ApiResponse<List<MenuDto>> getMenus();

    /**
     * 메뉴를 등록한다.
     */
    ApiResponse<Void> createMenu(MenuDto paramDto);

    /**
     * 메뉴 정보를 수정한다.
     */
    ApiResponse<Void> updateMenu(Long menuId, MenuDto paramDto);

    /**
     * 메뉴에 연결된 기능 목록을 조회한다.
     */
    ApiResponse<List<MenuActionDto>> getMenuActions(Long menuId);

    /**
     * 역할에 부여된 메뉴 목록을 조회한다.
     */
    ApiResponse<List<MenuDto>> getMenusByRoleId(Long roleId);

    /**
     * 역할별 메뉴 권한을 저장한다.
     */
    ApiResponse<Void> saveRoleMenus(Long roleId, RoleMenuSaveDto paramDto);
}
