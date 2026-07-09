package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.RoleMenuSaveReqDto;
import com.bwg.channel.backend.systemsvc.menu.repository.MenuRepository;
import com.bwg.channel.backend.systemsvc.menu.service.MenuService;
import com.bwg.channel.backend.systemsvc.menu.service.MenuServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 메뉴 기준정보와 역할별 메뉴 권한 서비스 흐름 검증
 */
class MenuServiceTests {

    private final MenuRepository menuRepository = mock(MenuRepository.class);
    private final MenuService menuService = new MenuServiceImpl(menuRepository);

    @Test
    void returnsMenuByIdWithoutPaginationMetadata() {
        MenuResDto menu = new MenuResDto();
        menu.setMenuId(1L);
        menu.setMenuCd("DASHBOARD");
        menu.setMenuNm("대시보드");

        when(menuRepository.findMenu(1L)).thenReturn(menu);

        ApiResponse<MenuResDto> response = menuService.getMenu(1L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPayload().getMenuId()).isEqualTo(1L);
        assertThat(response.getPayload().getMenuCd()).isEqualTo("DASHBOARD");
        assertThat(response.getPagination()).isNull();
        verify(menuRepository).findMenu(1L);
    }

    @Test
    void rejectsMissingMenuDetail() {
        when(menuRepository.findMenu(99L)).thenReturn(null);

        assertThatThrownBy(() -> menuService.getMenu(99L))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.REQUIRED_VALUE_MISSING);
        verify(menuRepository).findMenu(99L);
    }

    @Test
    void returnsMenusByRoleId() {
        MenuResDto menu = new MenuResDto();
        menu.setMenuId(1L);
        menu.setMenuCd("DASHBOARD");
        menu.setMenuNm("대시보드");

        when(menuRepository.findMenusByRoleId(1L)).thenReturn(List.of(menu));

        ApiResponse<List<MenuResDto>> response = menuService.getMenusByRoleId(1L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPayload()).extracting(MenuResDto::getMenuCd)
                .containsExactly("DASHBOARD");
        assertThat(response.getPagination().getPage()).isEqualTo(1);
        assertThat(response.getPagination().getSize()).isEqualTo(1);
        assertThat(response.getPagination().getTotalCount()).isEqualTo(1L);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
    }

    @Test
    void returnsMenusWithPaginationMetadata() {
        MenuResDto firstMenu = new MenuResDto();
        firstMenu.setMenuCd("DASHBOARD");
        MenuResDto secondMenu = new MenuResDto();
        secondMenu.setMenuCd("PRODUCT");

        when(menuRepository.findMenus()).thenReturn(List.of(firstMenu, secondMenu));

        ApiResponse<List<MenuResDto>> response = menuService.getMenus();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPayload()).extracting(MenuResDto::getMenuCd)
                .containsExactly("DASHBOARD", "PRODUCT");
        assertThat(response.getPagination().getPage()).isEqualTo(1);
        assertThat(response.getPagination().getSize()).isEqualTo(2);
        assertThat(response.getPagination().getTotalCount()).isEqualTo(2L);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
    }

    @Test
    void replacesRoleMenus() {
        RoleMenuSaveReqDto paramDto = new RoleMenuSaveReqDto();
        paramDto.setMenuIds(List.of(1L, 2L));
        paramDto.setCreatedBy("admin");
        ApiRequest<RoleMenuSaveReqDto> request = new ApiRequest<>();
        request.setData(paramDto);

        ApiResponse<Void> response = menuService.saveRoleMenus(1L, request);

        assertThat(response.isSuccess()).isTrue();
        verify(menuRepository).deleteRoleMenus(1L);
        verify(menuRepository).insertRoleMenu(1L, 1L, "admin");
        verify(menuRepository).insertRoleMenu(1L, 2L, "admin");
    }
}
