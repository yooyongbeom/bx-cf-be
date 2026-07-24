package com.bwg.channel.backend.authsvc.user.service;

import com.bwg.channel.backend.authsvc.user.dto.UserCreateReqDto;
import com.bwg.channel.backend.authsvc.user.dto.UserDetailResDto;
import com.bwg.channel.backend.authsvc.user.dto.UserListResDto;
import com.bwg.channel.backend.authsvc.user.dto.UserUpdateReqDto;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;

import java.util.List;

/** 관리자 권한으로 사용자를 조회하고 변경하는 업무 계약. */
public interface UserManagementService {

    ApiResponse<List<UserListResDto>> getUsers(String roles);

    ApiResponse<UserDetailResDto> getUser(String userId, String roles);

    ApiResponse<Void> createUser(
            ApiRequest<UserCreateReqDto> request,
            String actor,
            String roles
    );

    ApiResponse<Void> updateUser(
            String userId,
            ApiRequest<UserUpdateReqDto> request,
            String actor,
            String roles
    );

    ApiResponse<Void> deleteUser(String userId, String actor, String roles);
}
