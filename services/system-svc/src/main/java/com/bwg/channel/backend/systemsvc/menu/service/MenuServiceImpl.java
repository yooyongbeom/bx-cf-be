package com.bwg.channel.backend.systemsvc.menu.service;

import com.bwg.channel.backend.businesscommon.validation.BusinessValidator;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.domain.dto.PaginationResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.RoleMenuSaveReqDto;
import com.bwg.channel.backend.systemsvc.menu.repository.MenuRepository;
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

    private final MenuRepository menuRepository;

    @Override
    public ApiResponse<List<MenuResDto>> getMenus() {
        // 저장소에서 전체 메뉴 목록 조회
        List<MenuResDto> result = menuRepository.findMenus();
        // 조회 결과에 목록 메타데이터를 포함하여 응답 생성
        return ApiResponse.success(result, toPagination(result));
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> createMenu(ApiRequest<MenuReqDto> paramDto) {
        // 요청 본문 데이터 필수 여부 검증
        MenuReqDto data = requireData(paramDto);
        // 등록 필수값 검증
        data.setMenuCd(BusinessValidator.requireNonBlank(data.getMenuCd(), "menuCd"));
        data.setMenuNm(BusinessValidator.requireNonBlank(data.getMenuNm(), "menuNm"));
        // 메뉴 등록 처리
        menuRepository.insertMenu(paramDto);
        return ApiResponse.success(null);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateMenu(Long menuId, ApiRequest<MenuReqDto> paramDto) {
        // 요청 본문 데이터 필수 여부 검증
        MenuReqDto data = requireData(paramDto);
        // 경로 변수와 수정 필수값 검증
        data.setMenuId(BusinessValidator.requireNonNull(menuId, "menuId"));
        data.setMenuNm(BusinessValidator.requireNonBlank(data.getMenuNm(), "menuNm"));
        // 메뉴 수정 처리
        menuRepository.updateMenu(paramDto);
        return ApiResponse.success(null);
    }

    @Override
    public ApiResponse<List<MenuActionResDto>> getMenuActions(Long menuId) {
        // 메뉴 ID 검증 후 메뉴 기능 목록 조회
        List<MenuActionResDto> result = menuRepository.findMenuActions(BusinessValidator.requireNonNull(menuId, "menuId"));
        // 조회 결과에 목록 메타데이터를 포함하여 응답 생성
        return ApiResponse.success(result, toPagination(result));
    }

    @Override
    public ApiResponse<List<MenuResDto>> getMenusByRoleId(Long roleId) {
        // 역할 ID 검증 후 역할별 메뉴 목록 조회
        List<MenuResDto> result = menuRepository.findMenusByRoleId(BusinessValidator.requireNonNull(roleId, "roleId"));
        // 조회 결과에 목록 메타데이터를 포함하여 응답 생성
        return ApiResponse.success(result, toPagination(result));
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> saveRoleMenus(Long roleId, ApiRequest<RoleMenuSaveReqDto> paramDto) {
        // 역할 ID와 요청 본문 데이터 필수 여부 검증
        Long requiredRoleId = BusinessValidator.requireNonNull(roleId, "roleId");
        RoleMenuSaveReqDto data = requireData(paramDto);
        // 기존 역할별 메뉴 권한 삭제
        menuRepository.deleteRoleMenus(requiredRoleId);
        // 요청된 메뉴 ID 기준으로 역할별 메뉴 권한 재등록
        for (Long menuId : data.getMenuIds()) {
            menuRepository.insertRoleMenu(requiredRoleId, BusinessValidator.requireNonNull(menuId, "menuId"), data.getCreatedBy());
        }
        return ApiResponse.success(null);
    }

    private <T> T requireData(ApiRequest<T> request) {
        // 공통 요청 래퍼의 data 블록 검증
        return BusinessValidator.requireNonNull(request == null ? null : request.getData(), "data");
    }

    private PaginationResDto toPagination(List<?> result) {
        // 현재 전체 목록 응답 기준으로 페이지 메타데이터 생성
        int totalCount = result == null ? 0 : result.size();
        PaginationResDto pagination = new PaginationResDto();
        pagination.setPage(1);
        pagination.setSize(totalCount);
        pagination.setTotalCount((long) totalCount);
        pagination.setTotalPages(totalCount == 0 ? 0 : 1);
        return pagination;
    }
}
