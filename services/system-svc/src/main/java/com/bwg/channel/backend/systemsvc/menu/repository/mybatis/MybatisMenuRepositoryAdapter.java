package com.bwg.channel.backend.systemsvc.menu.repository.mybatis;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuCreateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuDetailResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuListResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuUpdateReqDto;
import com.bwg.channel.backend.systemsvc.menu.repository.MenuRepository;
import com.bwg.channel.backend.systemsvc.menu.repository.mybatis.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MenuRepository 계약을 MyBatis Mapper 호출로 연결하는 어댑터.
 */
@Repository("mybatisMenu")
@RequiredArgsConstructor
public class MybatisMenuRepositoryAdapter implements MenuRepository {

    private final MenuMapper menuMapper;

    @Override
    public List<MenuListResDto> findMenus() {
        // MyBatis Mapper를 통해 전체 메뉴 목록 조회
        return menuMapper.findMenus();
    }

    @Override
    public MenuDetailResDto findMenu(Long menuId) {
        // MyBatis Mapper를 통해 메뉴 상세 조회
        return menuMapper.findMenu(menuId);
    }

    @Override
    public int insertMenu(ApiRequest<MenuCreateReqDto> paramDto, String createdBy) {
        // MyBatis Mapper를 통해 메뉴 등록
        return menuMapper.insertMenu(paramDto, createdBy);
    }

    @Override
    public int updateMenu(Long menuId, ApiRequest<MenuUpdateReqDto> paramDto, String updatedBy) {
        // MyBatis Mapper를 통해 메뉴 수정
        return menuMapper.updateMenu(menuId, paramDto, updatedBy);
    }

    @Override
    public List<Long> findMenuHierarchyIds(Long menuId) {
        // MyBatis Mapper를 통해 루트 메뉴와 모든 하위 메뉴 ID 조회
        return menuMapper.findMenuHierarchyIds(menuId);
    }

    @Override
    public int deleteRoleMenusByMenuIds(List<Long> menuIds) {
        // MyBatis Mapper를 통해 메뉴별 역할 권한 일괄 삭제
        return menuMapper.deleteRoleMenusByMenuIds(menuIds);
    }

    @Override
    public int deleteMenuActionsByMenuIds(List<Long> menuIds) {
        // MyBatis Mapper를 통해 메뉴 기능 일괄 삭제
        return menuMapper.deleteMenuActionsByMenuIds(menuIds);
    }

    @Override
    public int deleteMenus(List<Long> menuIds) {
        // MyBatis Mapper를 통해 메뉴 계층 일괄 삭제
        return menuMapper.deleteMenus(menuIds);
    }

    @Override
    public List<MenuActionResDto> findMenuActions(Long menuId) {
        // MyBatis Mapper를 통해 메뉴 기능 목록 조회
        return menuMapper.findMenuActions(menuId);
    }

    @Override
    public List<MenuListResDto> findMenusByRoleId(Long roleId) {
        // MyBatis Mapper를 통해 역할별 메뉴 목록 조회
        return menuMapper.findMenusByRoleId(roleId);
    }

    @Override
    public int deleteRoleMenus(Long roleId) {
        // MyBatis Mapper를 통해 역할별 메뉴 권한 삭제
        return menuMapper.deleteRoleMenus(roleId);
    }

    @Override
    public int insertRoleMenu(Long roleId, Long menuId, String createdBy) {
        // MyBatis Mapper를 통해 역할별 메뉴 권한 등록
        return menuMapper.insertRoleMenu(roleId, menuId, createdBy);
    }

    @Override
    public int insertReferenceDataVersionHistory(
            String refType,
            String changeType,
            String targetTable,
            String targetId,
            String changeSummary,
            String changedBy
    ) {
        // MyBatis Mapper를 통해 기준정보 버전 변경 이력 등록
        return menuMapper.insertReferenceDataVersionHistory(
                refType,
                changeType,
                targetTable,
                targetId,
                changeSummary,
                changedBy
        );
    }

    @Override
    public int updateReferenceDataVersion(String refType, String remark, String changedBy) {
        // MyBatis Mapper를 통해 기준정보 최신 버전 갱신
        return menuMapper.updateReferenceDataVersion(refType, remark, changedBy);
    }
}
