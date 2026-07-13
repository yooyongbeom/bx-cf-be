package com.bwg.channel.backend.systemsvc.menu.repository.mybatis.mapper;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuCreateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuDetailResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuListResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuUpdateReqDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 메뉴와 역할별 메뉴 권한 SQL 매핑을 담당하는 MyBatis Mapper.
 */
@Mapper
public interface MenuMapper {

    /**
     * 전체 메뉴 목록 조회 SQL 매핑
     */
    List<MenuListResDto> findMenus();

    /**
     * 메뉴 ID 기준 상세 조회 SQL 매핑
     */
    MenuDetailResDto findMenu(@Param("menuId") Long menuId);

    /**
     * 메뉴 등록 SQL 매핑
     */
    int insertMenu(ApiRequest<MenuCreateReqDto> paramDto);

    /**
     * 메뉴 수정 SQL 매핑
     */
    int updateMenu(
            @Param("menuId") Long menuId,
            @Param("request") ApiRequest<MenuUpdateReqDto> paramDto
    );

    /**
     * 루트 메뉴와 모든 하위 메뉴 ID 조회 SQL 매핑
     */
    List<Long> findMenuHierarchyIds(@Param("menuId") Long menuId);

    /**
     * 메뉴 ID 목록에 연결된 역할별 메뉴 권한 삭제 SQL 매핑
     */
    int deleteRoleMenusByMenuIds(@Param("menuIds") List<Long> menuIds);

    /**
     * 메뉴 ID 목록에 연결된 메뉴 기능 삭제 SQL 매핑
     */
    int deleteMenuActionsByMenuIds(@Param("menuIds") List<Long> menuIds);

    /**
     * 메뉴 ID 목록 물리 삭제 SQL 매핑
     */
    int deleteMenus(@Param("menuIds") List<Long> menuIds);

    /**
     * 메뉴 ID 기준 기능 목록 조회 SQL 매핑
     */
    List<MenuActionResDto> findMenuActions(@Param("menuId") Long menuId);

    /**
     * 역할 ID 기준 메뉴 목록 조회 SQL 매핑
     */
    List<MenuListResDto> findMenusByRoleId(@Param("roleId") Long roleId);

    /**
     * 역할별 메뉴 권한 삭제 SQL 매핑
     */
    int deleteRoleMenus(@Param("roleId") Long roleId);

    /**
     * 역할별 메뉴 권한 등록 SQL 매핑
     */
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId, @Param("createdBy") String createdBy);

    /**
     * 기준정보 버전 변경 이력 등록 SQL 매핑
     */
    int insertReferenceDataVersionHistory(
            @Param("refType") String refType,
            @Param("changeType") String changeType,
            @Param("targetTable") String targetTable,
            @Param("targetId") String targetId,
            @Param("changeSummary") String changeSummary,
            @Param("changedBy") String changedBy
    );

    /**
     * 기준정보 최신 버전 갱신 SQL 매핑
     */
    int updateReferenceDataVersion(
            @Param("refType") String refType,
            @Param("remark") String remark,
            @Param("changedBy") String changedBy
    );
}
