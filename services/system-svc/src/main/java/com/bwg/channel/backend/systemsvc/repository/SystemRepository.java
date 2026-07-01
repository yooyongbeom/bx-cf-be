package com.bwg.channel.backend.systemsvc.repository;

import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuResDto;

import java.util.List;

/**
 * 시스템 기준정보 데이터 접근을 추상화한 저장소 계약.
 */
public interface SystemRepository {

    List<CommonCodeGroupResDto> findCommonCodeGroups();

    int insertCommonCodeGroup(CommonCodeGroupReqDto paramDto);

    int updateCommonCodeGroup(CommonCodeGroupReqDto paramDto);

    List<CommonCodeResDto> findCommonCodes(String groupCd);

    int insertCommonCode(CommonCodeReqDto paramDto);

    int updateCommonCode(CommonCodeReqDto paramDto);

    List<MenuResDto> findMenus();

    int insertMenu(MenuReqDto paramDto);

    int updateMenu(MenuReqDto paramDto);

    List<MenuActionResDto> findMenuActions(Long menuId);

    List<MenuResDto> findMenusByRoleId(Long roleId);

    int deleteRoleMenus(Long roleId);

    int insertRoleMenu(Long roleId, Long menuId, String createdBy);
}
