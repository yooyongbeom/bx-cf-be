package com.bwg.channel.backend.systemsvc.repository.mybatis.mapper;

import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuActionDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * system-svc 기준정보 SQL 매핑을 담당하는 MyBatis Mapper.
 */
@Mapper
public interface SystemMapper {
    List<CommonCodeGroupDto> findCommonCodeGroups();
    int insertCommonCodeGroup(CommonCodeGroupDto paramDto);
    int updateCommonCodeGroup(CommonCodeGroupDto paramDto);

    List<CommonCodeDto> findCommonCodes(@Param("groupCd") String groupCd);
    int insertCommonCode(CommonCodeDto paramDto);
    int updateCommonCode(CommonCodeDto paramDto);

    List<MenuDto> findMenus();
    int insertMenu(MenuDto paramDto);
    int updateMenu(MenuDto paramDto);
    List<MenuActionDto> findMenuActions(@Param("menuId") Long menuId);

    List<MenuDto> findMenusByRoleId(@Param("roleId") Long roleId);
    int deleteRoleMenus(@Param("roleId") Long roleId);
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId, @Param("createdBy") String createdBy);
}
