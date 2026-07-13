package com.bwg.channel.backend.systemsvc.menu.service;

import com.bwg.channel.backend.businesscommon.validation.BusinessValidator;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.domain.dto.PaginationResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuCreateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuDeleteReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuDetailResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuListResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuUpdateReqDto;
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

    private static final String REF_TYPE_MENU = "MENU";

    private final MenuRepository menuRepository;

    @Override
    public ApiResponse<List<MenuListResDto>> getMenus() {
        // 저장소에서 전체 메뉴 목록 조회
        List<MenuListResDto> result = menuRepository.findMenus();
        // 조회 결과에 목록 메타데이터를 포함하여 응답 생성
        return ApiResponse.success(result, toPagination(result));
    }

    @Override
    public ApiResponse<MenuDetailResDto> getMenu(Long menuId) {
        // 메뉴 ID 검증 후 단건 메뉴 상세 조회, 미존재 시 404 응답
        Long requiredMenuId = BusinessValidator.requireNonNull(menuId, "menuId");
        MenuDetailResDto result = BusinessValidator.requireFound(menuRepository.findMenu(requiredMenuId), "menu");
        return ApiResponse.success(result);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> createMenu(ApiRequest<MenuCreateReqDto> paramDto) {
        // 요청 본문 데이터 필수 여부 검증
        MenuCreateReqDto data = requireData(paramDto);
        // 등록 필수값 검증
        data.setMenuCd(BusinessValidator.requireNonBlank(data.getMenuCd(), "menuCd"));
        data.setMenuNm(BusinessValidator.requireNonBlank(data.getMenuNm(), "menuNm"));
        // 메뉴 등록 처리
        menuRepository.insertMenu(paramDto);
        recordMenuVersionChange(
                "CREATE",
                "menus",
                data.getMenuCd(),
                "메뉴 등록",
                data.getCreatedBy()
        );
        return ApiResponse.success(null);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateMenu(Long menuId, ApiRequest<MenuUpdateReqDto> paramDto) {
        // 요청 본문 데이터 필수 여부 검증
        MenuUpdateReqDto data = requireData(paramDto);
        // 경로 변수와 수정 필수값 검증
        Long requiredMenuId = BusinessValidator.requireNonNull(menuId, "menuId");
        data.setMenuNm(BusinessValidator.requireNonBlank(data.getMenuNm(), "menuNm"));
        // 메뉴 수정 처리
        menuRepository.updateMenu(requiredMenuId, paramDto);
        recordMenuVersionChange(
                "UPDATE",
                "menus",
                String.valueOf(requiredMenuId),
                "메뉴 수정",
                data.getCreatedBy()
        );
        return ApiResponse.success(null);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> deleteMenu(Long menuId, ApiRequest<MenuDeleteReqDto> paramDto) {
        // 삭제 대상과 작업자 필수값 검증
        Long requiredMenuId = BusinessValidator.requireNonNull(menuId, "menuId");
        MenuDeleteReqDto data = requireData(paramDto);
        String deletedBy = BusinessValidator.requireNonBlank(data.getDeletedBy(), "deletedBy");
        // 루트 메뉴와 모든 하위 메뉴 ID를 한 번에 조회하고 미존재 여부 검증
        List<Long> menuIds = menuRepository.findMenuHierarchyIds(requiredMenuId);
        List<Long> requiredMenuIds = BusinessValidator.requireFound(
                menuIds == null || menuIds.isEmpty() ? null : menuIds,
                "menu"
        );
        // 외래키 연결 데이터를 먼저 제거한 뒤 메뉴 계층 전체 삭제
        menuRepository.deleteRoleMenusByMenuIds(requiredMenuIds);
        menuRepository.deleteMenuActionsByMenuIds(requiredMenuIds);
        menuRepository.deleteMenus(requiredMenuIds);
        recordMenuVersionChange(
                "DELETE",
                "menus",
                String.valueOf(requiredMenuId),
                "메뉴 삭제",
                deletedBy
        );
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
    public ApiResponse<List<MenuListResDto>> getMenusByRoleId(Long roleId) {
        // 역할 ID 검증 후 역할별 메뉴 목록 조회
        List<MenuListResDto> result = menuRepository.findMenusByRoleId(BusinessValidator.requireNonNull(roleId, "roleId"));
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
        recordMenuVersionChange(
                "SAVE",
                "role_menus",
                String.valueOf(requiredRoleId),
                "역할별 메뉴 권한 저장",
                data.getCreatedBy()
        );
        return ApiResponse.success(null);
    }

    private void recordMenuVersionChange(
            String changeType,
            String targetTable,
            String targetId,
            String changeSummary,
            String changedBy
    ) {
        // 업무 데이터 변경과 같은 트랜잭션에서 기준정보 버전 이력과 최신 버전을 함께 갱신
        menuRepository.insertReferenceDataVersionHistory(
                REF_TYPE_MENU,
                changeType,
                targetTable,
                targetId,
                changeSummary,
                changedBy
        );
        menuRepository.updateReferenceDataVersion(REF_TYPE_MENU, changeSummary, changedBy);
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
