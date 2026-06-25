package com.bwg.channel.backend.systemsvc.controller;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuActionDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuDto;
import com.bwg.channel.backend.systemsvc.domain.dto.RoleMenuSaveDto;
import com.bwg.channel.backend.systemsvc.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 메뉴와 역할별 메뉴 권한 관리 API를 제공하는 컨트롤러.
 */
@Tag(name = "기준정보-메뉴")
@RestController
@RequiredArgsConstructor
@RequestMapping("/menus")
public class MenuController {

    private final MenuService menuService;

    /**
     * 전체 메뉴 목록을 조회한다.
     */
    @Operation(summary = "메뉴 목록 조회")
    @PostMapping("/list")
    public ApiResponse<List<MenuDto>> getMenus() {
        return menuService.getMenus();
    }

    /**
     * 신규 메뉴를 등록한다.
     */
    @Operation(summary = "메뉴 등록")
    @PostMapping("/create")
    public ApiResponse<Void> createMenu(@RequestBody MenuDto paramDto) {
        return menuService.createMenu(paramDto);
    }

    /**
     * 경로의 메뉴 ID를 기준으로 메뉴 정보를 수정한다.
     */
    @Operation(summary = "메뉴 수정")
    @PostMapping("/{menuId}/update")
    public ApiResponse<Void> updateMenu(@PathVariable Long menuId, @RequestBody MenuDto paramDto) {
        return menuService.updateMenu(menuId, paramDto);
    }

    /**
     * 특정 메뉴에 연결된 기능 목록을 조회한다.
     */
    @Operation(summary = "메뉴 기능 목록 조회")
    @PostMapping("/{menuId}/actions/list")
    public ApiResponse<List<MenuActionDto>> getMenuActions(@PathVariable Long menuId) {
        return menuService.getMenuActions(menuId);
    }

    /**
     * 역할에 부여된 메뉴 목록을 조회한다.
     */
    @Operation(summary = "역할별 메뉴 목록 조회")
    @PostMapping("/roles/{roleId}/list")
    public ApiResponse<List<MenuDto>> getMenusByRoleId(@PathVariable Long roleId) {
        return menuService.getMenusByRoleId(roleId);
    }

    /**
     * 역할에 매핑된 메뉴 권한을 전체 교체 방식으로 저장한다.
     */
    @Operation(summary = "역할별 메뉴 권한 저장")
    @PostMapping("/roles/{roleId}/save")
    public ApiResponse<Void> saveRoleMenus(
            @PathVariable Long roleId,
            @RequestBody RoleMenuSaveDto paramDto
    ) {
        return menuService.saveRoleMenus(roleId, paramDto);
    }
}
