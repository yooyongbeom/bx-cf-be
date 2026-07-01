package com.bwg.channel.backend.systemsvc.controller;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.RoleMenuSaveReqDto;
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
 * 메뉴와 역할별 메뉴 권한 관리 API 컨트롤러. (요청 {@code *ReqDto} / 응답 {@code *ResDto}를 그대로 사용)
 */
@Tag(name = "기준정보-메뉴")
@RestController
@RequiredArgsConstructor
@RequestMapping("/menus")
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "메뉴 목록 조회")
    @PostMapping("/list")
    public ApiResponse<List<MenuResDto>> getMenus() {
        return menuService.getMenus();
    }

    @Operation(summary = "메뉴 등록")
    @PostMapping("/create")
    public ApiResponse<Void> createMenu(@RequestBody MenuReqDto req) {
        return menuService.createMenu(req);
    }

    @Operation(summary = "메뉴 수정")
    @PostMapping("/{menuId}/update")
    public ApiResponse<Void> updateMenu(@PathVariable Long menuId, @RequestBody MenuReqDto req) {
        return menuService.updateMenu(menuId, req);
    }

    @Operation(summary = "메뉴 기능 목록 조회")
    @PostMapping("/{menuId}/actions/list")
    public ApiResponse<List<MenuActionResDto>> getMenuActions(@PathVariable Long menuId) {
        return menuService.getMenuActions(menuId);
    }

    @Operation(summary = "역할별 메뉴 목록 조회")
    @PostMapping("/roles/{roleId}/list")
    public ApiResponse<List<MenuResDto>> getMenusByRoleId(@PathVariable Long roleId) {
        return menuService.getMenusByRoleId(roleId);
    }

    @Operation(summary = "역할별 메뉴 권한 저장")
    @PostMapping("/roles/{roleId}/save")
    public ApiResponse<Void> saveRoleMenus(
            @PathVariable Long roleId,
            @RequestBody RoleMenuSaveReqDto req
    ) {
        return menuService.saveRoleMenus(roleId, req);
    }
}
