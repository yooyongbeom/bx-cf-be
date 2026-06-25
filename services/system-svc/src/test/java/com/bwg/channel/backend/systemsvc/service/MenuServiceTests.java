package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuDto;
import com.bwg.channel.backend.systemsvc.domain.dto.RoleMenuSaveDto;
import com.bwg.channel.backend.systemsvc.repository.SystemRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 메뉴 기준정보와 역할별 메뉴 권한 서비스 흐름 검증
 */
class MenuServiceTests {

    private final SystemRepository systemRepository = mock(SystemRepository.class);
    private final MenuService menuService = new MenuServiceImpl(systemRepository);

    @Test
    void returnsMenusByRoleId() {
        MenuDto menu = new MenuDto();
        menu.setMenuId(1L);
        menu.setMenuCd("DASHBOARD");
        menu.setMenuNm("대시보드");

        when(systemRepository.findMenusByRoleId(1L)).thenReturn(List.of(menu));

        ApiResponse<List<MenuDto>> response = menuService.getMenusByRoleId(1L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPayload()).extracting(MenuDto::getMenuCd)
                .containsExactly("DASHBOARD");
    }

    @Test
    void replacesRoleMenus() {
        RoleMenuSaveDto paramDto = new RoleMenuSaveDto();
        paramDto.setMenuIds(List.of(1L, 2L));
        paramDto.setCreatedBy("admin");

        ApiResponse<Void> response = menuService.saveRoleMenus(1L, paramDto);

        assertThat(response.isSuccess()).isTrue();
        verify(systemRepository).deleteRoleMenus(1L);
        verify(systemRepository).insertRoleMenu(1L, 1L, "admin");
        verify(systemRepository).insertRoleMenu(1L, 2L, "admin");
    }
}
