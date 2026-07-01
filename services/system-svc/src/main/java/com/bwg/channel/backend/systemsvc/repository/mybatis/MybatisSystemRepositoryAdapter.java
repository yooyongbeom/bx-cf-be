package com.bwg.channel.backend.systemsvc.repository.mybatis;

import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuResDto;
import com.bwg.channel.backend.systemsvc.repository.SystemRepository;
import com.bwg.channel.backend.systemsvc.repository.mybatis.mapper.SystemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SystemRepository 계약을 MyBatis Mapper 호출로 연결하는 어댑터.
 */
@Repository("mybatisSystem")
@RequiredArgsConstructor
public class MybatisSystemRepositoryAdapter implements SystemRepository {

    private final SystemMapper systemMapper;

    @Override
    public List<CommonCodeGroupResDto> findCommonCodeGroups() {
        return systemMapper.findCommonCodeGroups();
    }

    @Override
    public int insertCommonCodeGroup(CommonCodeGroupReqDto paramDto) {
        return systemMapper.insertCommonCodeGroup(paramDto);
    }

    @Override
    public int updateCommonCodeGroup(CommonCodeGroupReqDto paramDto) {
        return systemMapper.updateCommonCodeGroup(paramDto);
    }

    @Override
    public List<CommonCodeResDto> findCommonCodes(String groupCd) {
        return systemMapper.findCommonCodes(groupCd);
    }

    @Override
    public int insertCommonCode(CommonCodeReqDto paramDto) {
        return systemMapper.insertCommonCode(paramDto);
    }

    @Override
    public int updateCommonCode(CommonCodeReqDto paramDto) {
        return systemMapper.updateCommonCode(paramDto);
    }

    @Override
    public List<MenuResDto> findMenus() {
        return systemMapper.findMenus();
    }

    @Override
    public int insertMenu(MenuReqDto paramDto) {
        return systemMapper.insertMenu(paramDto);
    }

    @Override
    public int updateMenu(MenuReqDto paramDto) {
        return systemMapper.updateMenu(paramDto);
    }

    @Override
    public List<MenuActionResDto> findMenuActions(Long menuId) {
        return systemMapper.findMenuActions(menuId);
    }

    @Override
    public List<MenuResDto> findMenusByRoleId(Long roleId) {
        return systemMapper.findMenusByRoleId(roleId);
    }

    @Override
    public int deleteRoleMenus(Long roleId) {
        return systemMapper.deleteRoleMenus(roleId);
    }

    @Override
    public int insertRoleMenu(Long roleId, Long menuId, String createdBy) {
        return systemMapper.insertRoleMenu(roleId, menuId, createdBy);
    }
}
