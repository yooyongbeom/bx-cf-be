package com.bwg.channel.backend.systemsvc.menu.repository;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuResDto;

import java.util.List;

/**
 * 메뉴와 역할별 메뉴 권한 데이터 접근을 추상화한 저장소 계약.
 */
public interface MenuRepository {

    /**
     * 전체 메뉴 목록 조회
     */
    List<MenuResDto> findMenus();

    /**
     * 메뉴 ID 기준 상세 조회
     */
    MenuResDto findMenu(Long menuId);

    /**
     * 메뉴 등록
     */
    int insertMenu(ApiRequest<MenuReqDto> paramDto);

    /**
     * 메뉴 수정
     */
    int updateMenu(ApiRequest<MenuReqDto> paramDto);

    /**
     * 메뉴 ID 기준 기능 목록 조회
     */
    List<MenuActionResDto> findMenuActions(Long menuId);

    /**
     * 역할 ID 기준 메뉴 목록 조회
     */
    List<MenuResDto> findMenusByRoleId(Long roleId);

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
