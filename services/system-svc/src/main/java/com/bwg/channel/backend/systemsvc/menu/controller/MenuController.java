package com.bwg.channel.backend.systemsvc.menu.controller;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuCreateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuDetailResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuListResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuUpdateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.RoleMenuSaveReqDto;
import com.bwg.channel.backend.systemsvc.menu.service.MenuService;
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
 * 메뉴와 역할별 메뉴 권한 관리 API 컨트롤러.
 */
@Tag(name = "기준정보-메뉴")
@RestController
@RequiredArgsConstructor
@RequestMapping("/menus")
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "메뉴 목록 조회")
    @PostMapping("/list")
    public ApiResponse<List<MenuListResDto>> getMenus() {
        // 전체 메뉴 목록 조회를 서비스에 위임
        return menuService.getMenus();
    }

    @Operation(summary = "메뉴 상세 조회")
    @PostMapping("/{menuId}/detail")
    public ApiResponse<MenuDetailResDto> getMenu(@PathVariable Long menuId) {
        // 메뉴 ID 기준 상세 조회를 서비스에 위임
        return menuService.getMenu(menuId);
    }

    @Operation(summary = "메뉴 등록")
    @PostMapping("/create")
    public ApiResponse<Void> createMenu(@RequestBody ApiRequest<MenuCreateReqDto> req) {
        // 메뉴 등록 요청을 서비스에 위임
        return menuService.createMenu(req);
    }

    @Operation(summary = "메뉴 수정")
    @PostMapping("/{menuId}/update")
    public ApiResponse<Void> updateMenu(@PathVariable Long menuId, @RequestBody ApiRequest<MenuUpdateReqDto> req) {
        // 메뉴 ID와 요청 본문을 함께 서비스에 전달
        return menuService.updateMenu(menuId, req);
    }

    @Operation(summary = "메뉴 기능 목록 조회")
    @PostMapping("/{menuId}/actions/list")
    public ApiResponse<List<MenuActionResDto>> getMenuActions(@PathVariable Long menuId) {
        // 메뉴 ID 기준 기능 목록 조회를 서비스에 위임
        return menuService.getMenuActions(menuId);
    }

    @Operation(summary = "역할별 메뉴 목록 조회")
    @PostMapping("/roles/{roleId}/list")
    public ApiResponse<List<MenuListResDto>> getMenusByRoleId(@PathVariable Long roleId) {
        // 역할 ID 기준 메뉴 목록 조회를 서비스에 위임
        return menuService.getMenusByRoleId(roleId);
    }

    @Operation(summary = "역할별 메뉴 권한 저장")
    @PostMapping("/roles/{roleId}/save")
    public ApiResponse<Void> saveRoleMenus(
            @PathVariable Long roleId,
            @RequestBody ApiRequest<RoleMenuSaveReqDto> req
    ) {
        // 역할 ID와 메뉴 권한 저장 요청을 서비스에 전달
        return menuService.saveRoleMenus(roleId, req);
    }
}
