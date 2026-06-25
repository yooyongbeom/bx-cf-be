package com.bwg.channel.backend.systemsvc.repository.mybatis;

import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuActionDto;
import com.bwg.channel.backend.systemsvc.domain.dto.MenuDto;
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
    public List<CommonCodeGroupDto> findCommonCodeGroups() {
        return systemMapper.findCommonCodeGroups();
    }

    @Override
    public int insertCommonCodeGroup(CommonCodeGroupDto paramDto) {
        return systemMapper.insertCommonCodeGroup(paramDto);
    }

    @Override
    public int updateCommonCodeGroup(CommonCodeGroupDto paramDto) {
        return systemMapper.updateCommonCodeGroup(paramDto);
    }

    @Override
    public List<CommonCodeDto> findCommonCodes(String groupCd) {
        return systemMapper.findCommonCodes(groupCd);
    }

    @Override
    public int insertCommonCode(CommonCodeDto paramDto) {
        return systemMapper.insertCommonCode(paramDto);
    }

    @Override
    public int updateCommonCode(CommonCodeDto paramDto) {
        return systemMapper.updateCommonCode(paramDto);
    }

    @Override
    public List<MenuDto> findMenus() {
        return systemMapper.findMenus();
    }

    @Override
    public int insertMenu(MenuDto paramDto) {
        return systemMapper.insertMenu(paramDto);
    }

    @Override
    public int updateMenu(MenuDto paramDto) {
        return systemMapper.updateMenu(paramDto);
    }

    @Override
    public List<MenuActionDto> findMenuActions(Long menuId) {
        return systemMapper.findMenuActions(menuId);
    }

    @Override
    public List<MenuDto> findMenusByRoleId(Long roleId) {
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
