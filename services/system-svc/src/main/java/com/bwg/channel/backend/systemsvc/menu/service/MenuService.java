package com.bwg.channel.backend.systemsvc.menu.service;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuCreateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuDetailResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuListResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuUpdateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.RoleMenuSaveReqDto;

import java.util.List;

/**
 * 메뉴와 역할별 메뉴 권한 관리 기능의 서비스 계약.
 */
public interface MenuService {

    /**
     * 전체 메뉴 목록을 조회한다.
     *
     * @return 전체 메뉴 목록과 페이지 정보가 포함된 응답
     */
    ApiResponse<List<MenuListResDto>> getMenus();

    /**
     * 메뉴 ID에 해당하는 메뉴 상세정보를 조회한다.
     *
     * @param menuId 조회할 메뉴 ID
     * @return 메뉴 상세정보가 포함된 응답
     */
    ApiResponse<MenuDetailResDto> getMenu(Long menuId);

    /**
     * 인증 사용자를 변경 주체로 기록하며 메뉴를 등록한다.
     *
     * @param paramDto 등록할 메뉴 정보
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 등록 성공 응답
     */
    ApiResponse<Void> createMenu(ApiRequest<MenuCreateReqDto> paramDto, String userId);

    /**
     * 메뉴 ID에 해당하는 메뉴 정보를 수정한다.
     *
     * @param menuId 수정할 메뉴 ID
     * @param paramDto 수정할 메뉴 정보
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 수정 성공 응답
     */
    ApiResponse<Void> updateMenu(Long menuId, ApiRequest<MenuUpdateReqDto> paramDto, String userId);

    /**
     * 지정한 메뉴와 모든 하위 메뉴 및 연결 데이터를 물리 삭제한다.
     *
     * @param menuId 삭제할 최상위 메뉴 ID
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 삭제 성공 응답
     */
    ApiResponse<Void> deleteMenu(Long menuId, String userId);

    /**
     * 메뉴 ID에 연결된 기능 목록을 조회한다.
     *
     * @param menuId 기능 목록을 조회할 메뉴 ID
     * @return 메뉴 기능 목록과 페이지 정보가 포함된 응답
     */
    ApiResponse<List<MenuActionResDto>> getMenuActions(Long menuId);

    /**
     * 역할 ID에 권한으로 연결된 메뉴 목록을 조회한다.
     *
     * @param roleId 메뉴 권한을 조회할 역할 ID
     * @return 역할별 메뉴 목록과 페이지 정보가 포함된 응답
     */
    ApiResponse<List<MenuListResDto>> getMenusByRoleId(Long roleId);

    /**
     * 역할의 기존 메뉴 권한을 제거하고 요청된 메뉴 목록으로 다시 저장한다.
     *
     * @param roleId 메뉴 권한을 저장할 역할 ID
     * @param paramDto 역할에 부여할 메뉴 ID 목록
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 저장 성공 응답
     */
    ApiResponse<Void> saveRoleMenus(Long roleId, ApiRequest<RoleMenuSaveReqDto> paramDto, String userId);
}
