package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuCreateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuDetailResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuListResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuUpdateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.RoleMenuSaveReqDto;
import com.bwg.channel.backend.systemsvc.menu.repository.MenuRepository;
import com.bwg.channel.backend.systemsvc.menu.service.MenuService;
import com.bwg.channel.backend.systemsvc.menu.service.MenuServiceImpl;
import com.bwg.channel.backend.systemsvc.referencedata.service.ReferenceDataVersionService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 메뉴 기준정보와 역할별 메뉴 권한 서비스 흐름 검증
 */
class MenuServiceTests {

    private final MenuRepository menuRepository = mock(MenuRepository.class);
    private final ReferenceDataVersionService referenceDataVersionService =
            mock(ReferenceDataVersionService.class);
    private final MenuService menuService =
            new MenuServiceImpl(menuRepository, referenceDataVersionService);

    @Test
    void returnsMenuByIdWithoutPaginationMetadata() {
        MenuDetailResDto menu = new MenuDetailResDto();
        menu.setMenuId(1L);
        menu.setMenuCd("DASHBOARD");
        menu.setMenuNm("대시보드");

        when(menuRepository.findMenu(1L)).thenReturn(menu);

        ApiResponse<MenuDetailResDto> response = menuService.getMenu(1L);

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
                .isEqualTo(BusinessErrorCode.BUSINESS_DATA_NOT_FOUND);
        verify(menuRepository).findMenu(99L);
    }

    @Test
    void returnsMenusByRoleId() {
        MenuListResDto menu = new MenuListResDto();
        menu.setMenuId(1L);
        menu.setMenuCd("DASHBOARD");
        menu.setMenuNm("대시보드");

        when(menuRepository.findMenusByRoleId(1L)).thenReturn(List.of(menu));

        ApiResponse<List<MenuListResDto>> response = menuService.getMenusByRoleId(1L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPayload()).extracting(MenuListResDto::getMenuCd)
                .containsExactly("DASHBOARD");
        assertThat(response.getPagination().getPage()).isEqualTo(1);
        assertThat(response.getPagination().getSize()).isEqualTo(1);
        assertThat(response.getPagination().getTotalCount()).isEqualTo(1L);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
    }

    @Test
    void returnsMenusWithPaginationMetadata() {
        MenuListResDto firstMenu = new MenuListResDto();
        firstMenu.setMenuCd("DASHBOARD");
        MenuListResDto secondMenu = new MenuListResDto();
        secondMenu.setMenuCd("PRODUCT");

        when(menuRepository.findMenus()).thenReturn(List.of(firstMenu, secondMenu));

        ApiResponse<List<MenuListResDto>> response = menuService.getMenus();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPayload()).extracting(MenuListResDto::getMenuCd)
                .containsExactly("DASHBOARD", "PRODUCT");
        assertThat(response.getPagination().getPage()).isEqualTo(1);
        assertThat(response.getPagination().getSize()).isEqualTo(2);
        assertThat(response.getPagination().getTotalCount()).isEqualTo(2L);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
    }

    @Test
    void createsMenuWithCreateDto() {
        MenuCreateReqDto data = new MenuCreateReqDto();
        data.setMenuCd(" DASHBOARD ");
        data.setMenuNm(" 대시보드 ");
        ApiRequest<MenuCreateReqDto> request = new ApiRequest<>();
        request.setData(data);

        ApiResponse<Void> response = menuService.createMenu(request, "hong.gildong");

        assertThat(response.isSuccess()).isTrue();
        assertThat(data.getMenuCd()).isEqualTo("DASHBOARD");
        assertThat(data.getMenuNm()).isEqualTo("대시보드");
        verify(menuRepository).insertMenu(request, "hong.gildong");
        verify(referenceDataVersionService).versionChange(
                "MENU", "CREATE", "menus", "DASHBOARD", "메뉴 등록", "hong.gildong");
    }

    @Test
    void updatesMenuWithPathIdAndUpdateDto() {
        MenuUpdateReqDto data = new MenuUpdateReqDto();
        data.setMenuNm(" 대시보드 ");
        ApiRequest<MenuUpdateReqDto> request = new ApiRequest<>();
        request.setData(data);

        ApiResponse<Void> response = menuService.updateMenu(7L, request, "hong.gildong");

        assertThat(response.isSuccess()).isTrue();
        assertThat(data.getMenuNm()).isEqualTo("대시보드");
        verify(menuRepository).updateMenu(7L, request, "hong.gildong");
        verify(referenceDataVersionService).versionChange(
                "MENU", "UPDATE", "menus", "7", "메뉴 수정", "hong.gildong");
    }

    @Test
    void deletesMenuHierarchyAssociationsAndVersionInOrder() {
        List<Long> menuIds = List.of(10L, 11L, 12L);
        when(menuRepository.findMenuHierarchyIds(10L)).thenReturn(menuIds);

        ApiResponse<Void> response = menuService.deleteMenu(10L, "hong.gildong");

        assertThat(response.isSuccess()).isTrue();
        InOrder inOrder = inOrder(menuRepository, referenceDataVersionService);
        inOrder.verify(menuRepository).findMenuHierarchyIds(10L);
        inOrder.verify(menuRepository).deleteRoleMenusByMenuIds(menuIds);
        inOrder.verify(menuRepository).deleteMenuActionsByMenuIds(menuIds);
        inOrder.verify(menuRepository).deleteMenus(menuIds);
        inOrder.verify(referenceDataVersionService).versionChange(
                "MENU", "DELETE", "menus", "10", "메뉴 삭제", "hong.gildong");
    }

    @Test
    void rejectsMissingMenuHierarchyBeforeDelete() {
        when(menuRepository.findMenuHierarchyIds(99L)).thenReturn(List.of());

        assertThatThrownBy(() -> menuService.deleteMenu(99L, "hong.gildong"))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.BUSINESS_DATA_NOT_FOUND);

        verify(menuRepository, never()).deleteRoleMenusByMenuIds(anyList());
        verify(menuRepository, never()).deleteMenuActionsByMenuIds(anyList());
        verify(menuRepository, never()).deleteMenus(anyList());
    }

    @Test
    void replacesRoleMenus() {
        RoleMenuSaveReqDto paramDto = new RoleMenuSaveReqDto();
        paramDto.setMenuIds(List.of(1L, 2L));
        ApiRequest<RoleMenuSaveReqDto> request = new ApiRequest<>();
        request.setData(paramDto);

        ApiResponse<Void> response = menuService.saveRoleMenus(1L, request, "hong.gildong");

        assertThat(response.isSuccess()).isTrue();
        verify(menuRepository).deleteRoleMenus(1L);
        verify(menuRepository).insertRoleMenu(1L, 1L, "hong.gildong");
        verify(menuRepository).insertRoleMenu(1L, 2L, "hong.gildong");
        verify(referenceDataVersionService).versionChange(
                "MENU", "SAVE", "role_menus", "1", "역할별 메뉴 권한 저장", "hong.gildong");
    }

    @Test
    void rejectsBlankActorBeforeMenuWrites() {
        MenuCreateReqDto createData = new MenuCreateReqDto();
        createData.setMenuCd("DASHBOARD");
        createData.setMenuNm("대시보드");
        ApiRequest<MenuCreateReqDto> createRequest = new ApiRequest<>();
        createRequest.setData(createData);

        MenuUpdateReqDto updateData = new MenuUpdateReqDto();
        updateData.setMenuNm("대시보드");
        ApiRequest<MenuUpdateReqDto> updateRequest = new ApiRequest<>();
        updateRequest.setData(updateData);

        RoleMenuSaveReqDto roleMenuData = new RoleMenuSaveReqDto();
        roleMenuData.setMenuIds(List.of(1L));
        ApiRequest<RoleMenuSaveReqDto> roleMenuRequest = new ApiRequest<>();
        roleMenuRequest.setData(roleMenuData);

        assertThatThrownBy(() -> menuService.createMenu(createRequest, " "))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.REQUIRED_VALUE_MISSING);
        assertThatThrownBy(() -> menuService.updateMenu(1L, updateRequest, " "))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.REQUIRED_VALUE_MISSING);
        assertThatThrownBy(() -> menuService.deleteMenu(1L, " "))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.REQUIRED_VALUE_MISSING);
        assertThatThrownBy(() -> menuService.saveRoleMenus(1L, roleMenuRequest, " "))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.REQUIRED_VALUE_MISSING);

        verifyNoInteractions(menuRepository, referenceDataVersionService);
    }
}
