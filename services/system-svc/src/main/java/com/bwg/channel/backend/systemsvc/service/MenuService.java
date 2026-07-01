package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.RoleMenuSaveReqDto;

import java.util.List;

/**
 * 메뉴와 역할별 메뉴 권한 관리 기능의 서비스 계약.
 */
public interface MenuService {

    ApiResponse<List<MenuResDto>> getMenus();

    ApiResponse<Void> createMenu(MenuReqDto paramDto);

    ApiResponse<Void> updateMenu(Long menuId, MenuReqDto paramDto);

    ApiResponse<List<MenuActionResDto>> getMenuActions(Long menuId);

    ApiResponse<List<MenuResDto>> getMenusByRoleId(Long roleId);

    ApiResponse<Void> saveRoleMenus(Long roleId, RoleMenuSaveReqDto paramDto);
}
