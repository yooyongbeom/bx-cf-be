package com.bwg.channel.backend.systemsvc.repository;

import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuActionDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuDto;

import java.util.List;

/**
 * 시스템 기준정보 데이터 접근을 추상화한 저장소 계약.
 */
public interface SystemRepository {

    /**
     * 공통코드 그룹 목록을 조회한다.
     */
    List<CommonCodeGroupDto> findCommonCodeGroups();

    /**
     * 공통코드 그룹을 등록한다.
     */
    int insertCommonCodeGroup(CommonCodeGroupDto paramDto);

    /**
     * 공통코드 그룹을 수정한다.
     */
    int updateCommonCodeGroup(CommonCodeGroupDto paramDto);

    /**
     * 특정 그룹의 공통코드 목록을 조회한다.
     */
    List<CommonCodeDto> findCommonCodes(String groupCd);

    /**
     * 공통코드를 등록한다.
     */
    int insertCommonCode(CommonCodeDto paramDto);

    /**
     * 공통코드를 수정한다.
     */
    int updateCommonCode(CommonCodeDto paramDto);

    /**
     * 전체 메뉴 목록을 조회한다.
     */
    List<MenuDto> findMenus();

    /**
     * 메뉴를 등록한다.
     */
    int insertMenu(MenuDto paramDto);

    /**
     * 메뉴 정보를 수정한다.
     */
    int updateMenu(MenuDto paramDto);

    /**
     * 메뉴에 연결된 기능 목록을 조회한다.
     */
    List<MenuActionDto> findMenuActions(Long menuId);

    /**
     * 역할에 부여된 메뉴 목록을 조회한다.
     */
    List<MenuDto> findMenusByRoleId(Long roleId);

    /**
     * 역할에 매핑된 메뉴 권한을 모두 삭제한다.
     */
    int deleteRoleMenus(Long roleId);

    /**
     * 역할과 메뉴의 권한 매핑을 등록한다.
     */
    int insertRoleMenu(Long roleId, Long menuId, String createdBy);
}
