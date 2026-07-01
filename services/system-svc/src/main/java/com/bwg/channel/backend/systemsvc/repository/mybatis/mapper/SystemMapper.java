package com.bwg.channel.backend.systemsvc.repository.mybatis.mapper;

import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuResDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * system-svc 기준정보 SQL 매핑을 담당하는 MyBatis Mapper.
 */
@Mapper
public interface SystemMapper {
    List<CommonCodeGroupResDto> findCommonCodeGroups();
    int insertCommonCodeGroup(CommonCodeGroupReqDto paramDto);
    int updateCommonCodeGroup(CommonCodeGroupReqDto paramDto);

    List<CommonCodeResDto> findCommonCodes(@Param("groupCd") String groupCd);
    int insertCommonCode(CommonCodeReqDto paramDto);
    int updateCommonCode(CommonCodeReqDto paramDto);

    List<MenuResDto> findMenus();
    int insertMenu(MenuReqDto paramDto);
    int updateMenu(MenuReqDto paramDto);
    List<MenuActionResDto> findMenuActions(@Param("menuId") Long menuId);

    List<MenuResDto> findMenusByRoleId(@Param("roleId") Long roleId);
    int deleteRoleMenus(@Param("roleId") Long roleId);
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId, @Param("createdBy") String createdBy);
}
