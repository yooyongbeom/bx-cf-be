package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.businesscommon.validation.BusinessValidator;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.RoleMenuSaveReqDto;
import com.bwg.channel.backend.systemsvc.repository.SystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 메뉴와 역할별 메뉴 권한의 입력값을 검증하고 저장소 처리를 위임하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SystemRepository systemRepository;

    @Override
    public ApiResponse<List<MenuResDto>> getMenus() {
        return ApiResponse.success(systemRepository.findMenus());
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> createMenu(MenuReqDto paramDto) {
        paramDto.setMenuCd(BusinessValidator.requireNonBlank(paramDto.getMenuCd(), "menuCd"));
        paramDto.setMenuNm(BusinessValidator.requireNonBlank(paramDto.getMenuNm(), "menuNm"));
        systemRepository.insertMenu(paramDto);
        return ApiResponse.success(null);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateMenu(Long menuId, MenuReqDto paramDto) {
        paramDto.setMenuId(BusinessValidator.requireNonNull(menuId, "menuId"));
        paramDto.setMenuNm(BusinessValidator.requireNonBlank(paramDto.getMenuNm(), "menuNm"));
        systemRepository.updateMenu(paramDto);
        return ApiResponse.success(null);
    }

    @Override
    public ApiResponse<List<MenuActionResDto>> getMenuActions(Long menuId) {
        return ApiResponse.success(systemRepository.findMenuActions(BusinessValidator.requireNonNull(menuId, "menuId")));
    }

    @Override
    public ApiResponse<List<MenuResDto>> getMenusByRoleId(Long roleId) {
        return ApiResponse.success(systemRepository.findMenusByRoleId(BusinessValidator.requireNonNull(roleId, "roleId")));
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> saveRoleMenus(Long roleId, RoleMenuSaveReqDto paramDto) {
        Long requiredRoleId = BusinessValidator.requireNonNull(roleId, "roleId");
        systemRepository.deleteRoleMenus(requiredRoleId);
        for (Long menuId : paramDto.getMenuIds()) {
            systemRepository.insertRoleMenu(requiredRoleId, BusinessValidator.requireNonNull(menuId, "menuId"), paramDto.getCreatedBy());
        }
        return ApiResponse.success(null);
    }
}
