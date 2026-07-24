package com.bwg.channel.backend.authsvc.usermanagement.repository;

import com.bwg.channel.backend.authsvc.usermanagement.dto.UserCreateReqDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserDetailResDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserListResDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserUpdateReqDto;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;

import java.util.List;

/** 사용자 관리 데이터 접근 계약. */
public interface UserManagementRepository {

    List<UserListResDto> findUsers();

    UserDetailResDto findUser(String userId);

    boolean existsUser(String userId);

    /** 역할 이름으로 역할 ID를 조회해 기본 역할을 데이터베이스 기준으로 결정한다. */
    List<Long> findRoleIdsByName(String roleName);

    int insertUser(ApiRequest<UserCreateReqDto> request, String createdBy);

    int insertUserRole(String userId, Long roleId, String createdBy);

    int updateUser(String userId, ApiRequest<UserUpdateReqDto> request, String updatedBy);

    /** 사용자 본문 삭제 전에 FK 참조를 제거하기 위한 사용자-역할 물리 삭제이다. */
    int deleteUserRoles(String userId);

    int deleteUser(String userId);
}
