package com.bwg.channel.backend.systemsvc.menu.service;

import com.bwg.channel.backend.businesscommon.validation.BusinessValidator;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.domain.dto.PaginationResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuCreateReqDto;
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

    /**
     * 저장소의 전체 메뉴 목록을 페이지 메타데이터와 함께 반환한다.
     *
     * @return 전체 메뉴 목록과 페이지 정보가 포함된 응답
     */
    @Override
    public ApiResponse<List<MenuListResDto>> getMenus() {
        // 저장소에서 전체 메뉴 목록 조회
        List<MenuListResDto> result = menuRepository.findMenus();
        // 조회 결과에 목록 메타데이터를 포함하여 응답 생성
        return ApiResponse.success(result, toPagination(result));
    }

    /**
     * 메뉴 ID를 검증하고 해당 메뉴의 상세정보를 조회한다.
     *
     * @param menuId 조회할 메뉴 ID
     * @return 메뉴 상세정보가 포함된 응답
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException
     *         메뉴 ID가 없거나 대상 메뉴가 존재하지 않는 경우
     */
    @Override
    public ApiResponse<MenuDetailResDto> getMenu(Long menuId) {
        // 메뉴 ID 검증 후 단건 메뉴 상세 조회, 미존재 시 404 응답
        Long requiredMenuId = BusinessValidator.requireNonNull(menuId, "menuId");
        MenuDetailResDto result = BusinessValidator.requireFound(menuRepository.findMenu(requiredMenuId), "menu");
        return ApiResponse.success(result);
    }

    /**
     * 요청 데이터와 인증 사용자를 검증한 뒤 메뉴를 등록하고 메뉴 기준정보 버전을 갱신한다.
     *
     * <p>메뉴 등록과 버전 변경은 동일한 MyBatis 트랜잭션에서 수행한다.</p>
     *
     * @param paramDto 등록할 메뉴 정보
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 등록 성공 응답
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException
     *         요청 데이터, 메뉴 코드, 메뉴명 또는 사용자 ID가 없는 경우
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> createMenu(ApiRequest<MenuCreateReqDto> paramDto, String userId) {
        // 요청 본문 데이터 필수 여부 검증
        MenuCreateReqDto data = requireData(paramDto);
        String changedBy = requireActor(userId);
        // 등록 필수값 검증
        data.setMenuCd(BusinessValidator.requireNonBlank(data.getMenuCd(), "menuCd"));
        data.setMenuNm(BusinessValidator.requireNonBlank(data.getMenuNm(), "menuNm"));
        // 메뉴 등록 처리
        menuRepository.insertMenu(paramDto, changedBy);
        recordMenuVersionChange(
                "CREATE",
                "menus",
                data.getMenuCd(),
                "메뉴 등록",
                changedBy
        );
        return ApiResponse.success(null);
    }

    /**
     * 메뉴 ID와 수정 요청을 검증한 뒤 메뉴 정보와 기준정보 버전을 갱신한다.
     *
     * <p>메뉴 수정과 버전 변경은 동일한 MyBatis 트랜잭션에서 수행한다.</p>
     *
     * @param menuId 수정할 메뉴 ID
     * @param paramDto 수정할 메뉴 정보
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 수정 성공 응답
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException
     *         메뉴 ID, 요청 데이터, 메뉴명 또는 사용자 ID가 없는 경우
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateMenu(Long menuId, ApiRequest<MenuUpdateReqDto> paramDto, String userId) {
        // 요청 본문 데이터 필수 여부 검증
        MenuUpdateReqDto data = requireData(paramDto);
        String changedBy = requireActor(userId);
        // 경로 변수와 수정 필수값 검증
        Long requiredMenuId = BusinessValidator.requireNonNull(menuId, "menuId");
        data.setMenuNm(BusinessValidator.requireNonBlank(data.getMenuNm(), "menuNm"));
        // 메뉴 수정 처리
        menuRepository.updateMenu(requiredMenuId, paramDto, changedBy);
        recordMenuVersionChange(
                "UPDATE",
                "menus",
                String.valueOf(requiredMenuId),
                "메뉴 수정",
                changedBy
        );
        return ApiResponse.success(null);
    }

    /**
     * 지정한 메뉴의 전체 하위 계층을 조회해 연결된 역할 권한과 기능을 먼저 제거한 뒤 메뉴를 삭제한다.
     *
     * <p>연결 데이터 삭제, 메뉴 계층 삭제, 기준정보 버전 변경은 동일한 MyBatis 트랜잭션에서 수행한다.</p>
     *
     * @param menuId 삭제할 최상위 메뉴 ID
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 삭제 성공 응답
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException
     *         메뉴 ID나 사용자 ID가 없거나 대상 메뉴 계층이 존재하지 않는 경우
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> deleteMenu(Long menuId, String userId) {
        // 삭제 대상과 작업자 필수값 검증
        Long requiredMenuId = BusinessValidator.requireNonNull(menuId, "menuId");
        String changedBy = requireActor(userId);
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
                changedBy
        );
        return ApiResponse.success(null);
    }

    /**
     * 메뉴 ID에 연결된 기능 목록을 페이지 메타데이터와 함께 조회한다.
     *
     * @param menuId 기능 목록을 조회할 메뉴 ID
     * @return 메뉴 기능 목록과 페이지 정보가 포함된 응답
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException 메뉴 ID가 없는 경우
     */
    @Override
    public ApiResponse<List<MenuActionResDto>> getMenuActions(Long menuId) {
        // 메뉴 ID 검증 후 메뉴 기능 목록 조회
        List<MenuActionResDto> result = menuRepository.findMenuActions(BusinessValidator.requireNonNull(menuId, "menuId"));
        // 조회 결과에 목록 메타데이터를 포함하여 응답 생성
        return ApiResponse.success(result, toPagination(result));
    }

    /**
     * 역할 ID에 권한으로 연결된 메뉴 목록을 페이지 메타데이터와 함께 조회한다.
     *
     * @param roleId 메뉴 권한을 조회할 역할 ID
     * @return 역할별 메뉴 목록과 페이지 정보가 포함된 응답
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException 역할 ID가 없는 경우
     */
    @Override
    public ApiResponse<List<MenuListResDto>> getMenusByRoleId(Long roleId) {
        // 역할 ID 검증 후 역할별 메뉴 목록 조회
        List<MenuListResDto> result = menuRepository.findMenusByRoleId(BusinessValidator.requireNonNull(roleId, "roleId"));
        // 조회 결과에 목록 메타데이터를 포함하여 응답 생성
        return ApiResponse.success(result, toPagination(result));
    }

    /**
     * 역할의 기존 메뉴 권한을 삭제하고 요청된 메뉴 ID 목록을 순서대로 다시 등록한다.
     *
     * <p>기존 권한 삭제, 신규 권한 등록, 기준정보 버전 변경은 동일한 MyBatis 트랜잭션에서 수행한다.</p>
     *
     * @param roleId 메뉴 권한을 저장할 역할 ID
     * @param paramDto 역할에 부여할 메뉴 ID 목록
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 저장 성공 응답
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException
     *         역할 ID, 요청 데이터, 메뉴 ID 또는 사용자 ID가 없는 경우
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> saveRoleMenus(Long roleId, ApiRequest<RoleMenuSaveReqDto> paramDto, String userId) {
        // 역할 ID와 요청 본문 데이터 필수 여부 검증
        Long requiredRoleId = BusinessValidator.requireNonNull(roleId, "roleId");
        RoleMenuSaveReqDto data = requireData(paramDto);
        String changedBy = requireActor(userId);
        // 기존 역할별 메뉴 권한 삭제
        menuRepository.deleteRoleMenus(requiredRoleId);
        // 요청된 메뉴 ID 기준으로 역할별 메뉴 권한 재등록
        for (Long menuId : data.getMenuIds()) {
            menuRepository.insertRoleMenu(requiredRoleId, BusinessValidator.requireNonNull(menuId, "menuId"), changedBy);
        }
        recordMenuVersionChange(
                "SAVE",
                "role_menus",
                String.valueOf(requiredRoleId),
                "역할별 메뉴 권한 저장",
                changedBy
        );
        return ApiResponse.success(null);
    }

    /**
     * 메뉴 변경 이력을 등록하고 메뉴 기준정보의 최신 버전을 갱신한다.
     *
     * <p>호출한 메뉴 변경 작업의 트랜잭션에 참여한다.</p>
     *
     * @param changeType 변경 유형
     * @param targetTable 변경 대상 테이블
     * @param targetId 변경 대상 식별자
     * @param changeSummary 변경 내용 요약
     * @param changedBy 변경 사용자 ID
     */
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

    /**
     * 공통 API 요청 래퍼에서 필수 {@code data} 영역을 추출한다.
     *
     * @param request 공통 API 요청 래퍼
     * @param <T> 요청 데이터 타입
     * @return null이 아닌 요청 데이터
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException
     *         요청 또는 {@code data}가 없는 경우
     */
    private <T> T requireData(ApiRequest<T> request) {
        // 공통 요청 래퍼의 data 블록 검증
        return BusinessValidator.requireNonNull(request == null ? null : request.getData(), "data");
    }

    /**
     * Gateway가 전달한 인증 사용자 ID를 필수값으로 확인하고 정규화한다.
     *
     * @param userId Gateway의 {@code X-Auth-User} 헤더에서 전달된 사용자 ID
     * @return 공백이 제거된 사용자 ID
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException
     *         사용자 ID가 없거나 공백인 경우
     */
    private String requireActor(String userId) {
        // Gateway가 전달한 인증 사용자 ID 검증
        return BusinessValidator.requireNonBlank(userId, "userId");
    }

    /**
     * 전체 목록 조회 결과 크기를 기준으로 단일 페이지 메타데이터를 생성한다.
     *
     * @param result 페이지 정보를 계산할 조회 결과
     * @return 전체 결과를 한 페이지로 표현한 페이지 정보
     */
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
