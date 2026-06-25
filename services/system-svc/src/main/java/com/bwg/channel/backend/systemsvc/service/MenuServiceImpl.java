package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.businesscommon.validation.BusinessValidator;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuActionDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuDto;
import com.bwg.channel.backend.systemsvc.domain.dto.RoleMenuSaveDto;
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

    /**
     * 전체 메뉴 목록을 조회해 공통 응답으로 반환한다.
     */
    @Override
    public ApiResponse<List<MenuDto>> getMenus() {
        return ApiResponse.success(systemRepository.findMenus());
    }

    /**
     * 메뉴 코드와 메뉴명을 검증한 뒤 메뉴를 등록한다.
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> createMenu(MenuDto paramDto) {
        paramDto.setMenuCd(BusinessValidator.requireNonBlank(paramDto.getMenuCd(), "menuCd"));
        paramDto.setMenuNm(BusinessValidator.requireNonBlank(paramDto.getMenuNm(), "menuNm"));
        systemRepository.insertMenu(paramDto);
        return ApiResponse.success(null);
    }

    /**
     * 경로의 메뉴 ID를 적용하고 메뉴명을 검증한 뒤 메뉴 정보를 수정한다.
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateMenu(Long menuId, MenuDto paramDto) {
        paramDto.setMenuId(BusinessValidator.requireNonNull(menuId, "menuId"));
        paramDto.setMenuNm(BusinessValidator.requireNonBlank(paramDto.getMenuNm(), "menuNm"));
        systemRepository.updateMenu(paramDto);
        return ApiResponse.success(null);
    }

    /**
     * 메뉴 ID를 검증한 뒤 메뉴 기능 목록을 조회한다.
     */
    @Override
    public ApiResponse<List<MenuActionDto>> getMenuActions(Long menuId) {
        return ApiResponse.success(systemRepository.findMenuActions(BusinessValidator.requireNonNull(menuId, "menuId")));
    }

    /**
     * 역할 ID를 검증한 뒤 해당 역할에 부여된 메뉴 목록을 조회한다.
     */
    @Override
    public ApiResponse<List<MenuDto>> getMenusByRoleId(Long roleId) {
        return ApiResponse.success(systemRepository.findMenusByRoleId(BusinessValidator.requireNonNull(roleId, "roleId")));
    }

    /**
     * 기존 역할별 메뉴 권한을 삭제하고 요청 목록 기준으로 다시 저장한다.
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> saveRoleMenus(Long roleId, RoleMenuSaveDto paramDto) {
        Long requiredRoleId = BusinessValidator.requireNonNull(roleId, "roleId");
        systemRepository.deleteRoleMenus(requiredRoleId);
        for (Long menuId : paramDto.getMenuIds()) {
            systemRepository.insertRoleMenu(requiredRoleId, BusinessValidator.requireNonNull(menuId, "menuId"), paramDto.getCreatedBy());
        }
        return ApiResponse.success(null);
    }
}
