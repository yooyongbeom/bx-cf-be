package com.bwg.channel.backend.systemsvc.menu.repository.mybatis.mapper;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuResDto;
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
    List<MenuResDto> findMenus();

    /**
     * 메뉴 등록 SQL 매핑
     */
    int insertMenu(ApiRequest<MenuReqDto> paramDto);

    /**
     * 메뉴 수정 SQL 매핑
     */
    int updateMenu(ApiRequest<MenuReqDto> paramDto);

    /**
     * 메뉴 ID 기준 기능 목록 조회 SQL 매핑
     */
    List<MenuActionResDto> findMenuActions(@Param("menuId") Long menuId);

    /**
     * 역할 ID 기준 메뉴 목록 조회 SQL 매핑
     */
    List<MenuResDto> findMenusByRoleId(@Param("roleId") Long roleId);

    /**
     * 역할별 메뉴 권한 삭제 SQL 매핑
     */
    int deleteRoleMenus(@Param("roleId") Long roleId);

    /**
     * 역할별 메뉴 권한 등록 SQL 매핑
     */
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId, @Param("createdBy") String createdBy);
}
