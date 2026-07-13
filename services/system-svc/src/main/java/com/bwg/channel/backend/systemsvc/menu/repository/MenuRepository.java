package com.bwg.channel.backend.systemsvc.menu.repository;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuCreateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuDetailResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuListResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuUpdateReqDto;

import java.util.List;

/**
 * 메뉴와 역할별 메뉴 권한 데이터 접근을 추상화한 저장소 계약.
 */
public interface MenuRepository {

    /**
     * 전체 메뉴 목록 조회
     */
    List<MenuListResDto> findMenus();

    /**
     * 메뉴 ID 기준 상세 조회
     */
    MenuDetailResDto findMenu(Long menuId);

    /**
     * 메뉴 등록
     */
    int insertMenu(ApiRequest<MenuCreateReqDto> paramDto);

    /**
     * 메뉴 수정
     */
    int updateMenu(Long menuId, ApiRequest<MenuUpdateReqDto> paramDto);

    /**
     * 루트 메뉴와 모든 하위 메뉴 ID 조회
     */
    List<Long> findMenuHierarchyIds(Long menuId);

    /**
     * 메뉴 ID 목록에 연결된 역할별 메뉴 권한 삭제
     */
    int deleteRoleMenusByMenuIds(List<Long> menuIds);

    /**
     * 메뉴 ID 목록에 연결된 메뉴 기능 삭제
     */
    int deleteMenuActionsByMenuIds(List<Long> menuIds);

    /**
     * 메뉴 ID 목록 물리 삭제
     */
    int deleteMenus(List<Long> menuIds);

    /**
     * 메뉴 ID 기준 기능 목록 조회
     */
    List<MenuActionResDto> findMenuActions(Long menuId);

    /**
     * 역할 ID 기준 메뉴 목록 조회
     */
    List<MenuListResDto> findMenusByRoleId(Long roleId);

    /**
     * 역할별 메뉴 권한 전체 삭제
     */
    int deleteRoleMenus(Long roleId);

    /**
     * 역할별 메뉴 권한 등록
     */
    int insertRoleMenu(Long roleId, Long menuId, String createdBy);

    /**
     * 기준정보 버전 변경 이력 등록
     */
    int insertReferenceDataVersionHistory(
            String refType,
            String changeType,
            String targetTable,
            String targetId,
            String changeSummary,
            String changedBy
    );

    /**
     * 기준정보 최신 버전 갱신
     */
    int updateReferenceDataVersion(String refType, String remark, String changedBy);
}
