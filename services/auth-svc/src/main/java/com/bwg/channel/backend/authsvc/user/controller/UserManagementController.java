package com.bwg.channel.backend.authsvc.user.controller;

import com.bwg.channel.backend.authsvc.user.dto.UserCreateReqDto;
import com.bwg.channel.backend.authsvc.user.dto.UserDetailResDto;
import com.bwg.channel.backend.authsvc.user.dto.UserListResDto;
import com.bwg.channel.backend.authsvc.user.dto.UserUpdateReqDto;
import com.bwg.channel.backend.authsvc.user.service.UserManagementService;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.openapi.OpenApiSupport;
import com.bwg.channel.backend.securitycommon.constants.InternalAuthHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 관리자 사용자의 등록, 조회, 수정, 물리 삭제를 제공하는 관리자 API. */
@Tag(name = "사용자 관리")
@SecurityRequirement(name = OpenApiSupport.BEARER_SCHEME)
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @Operation(summary = "사용자 목록 조회")
    @PostMapping("/list")
    public ApiResponse<List<UserListResDto>> getUsers(
            @RequestHeader(value = InternalAuthHeaders.ROLES, required = false) String roles
    ) {
        // Gateway가 전달한 역할 정보를 서비스에 넘겨 관리자 권한 검증과 목록 조회를 수행한다.
        return userManagementService.getUsers(roles);
    }

    @Operation(summary = "사용자 상세 조회")
    @PostMapping("/{userId}/detail")
    public ApiResponse<UserDetailResDto> getUser(
            @PathVariable String userId,
            @RequestHeader(value = InternalAuthHeaders.ROLES, required = false) String roles
    ) {
        // 경로 사용자 ID와 검증된 역할 정보를 서비스에 전달해 권한 검증 후 상세를 조회한다.
        return userManagementService.getUser(userId, roles);
    }

    @Operation(summary = "사용자 등록")
    @PostMapping("/create")
    public ApiResponse<Void> createUser(
            @RequestBody ApiRequest<UserCreateReqDto> request,
            @RequestHeader(value = InternalAuthHeaders.USER, required = false) String actor,
            @RequestHeader(value = InternalAuthHeaders.ROLES, required = false) String roles
    ) {
        // Gateway가 검증해 주입한 작업자와 역할 정보를 등록 요청과 함께 서비스에 전달한다.
        return userManagementService.createUser(request, actor, roles);
    }

    @Operation(summary = "사용자 수정")
    @PostMapping("/{userId}/update")
    public ApiResponse<Void> updateUser(
            @PathVariable String userId,
            @RequestBody ApiRequest<UserUpdateReqDto> request,
            @RequestHeader(value = InternalAuthHeaders.USER, required = false) String actor,
            @RequestHeader(value = InternalAuthHeaders.ROLES, required = false) String roles
    ) {
        // 수정 대상과 Gateway가 보증한 작업자·역할 정보를 서비스에 그대로 전달한다.
        return userManagementService.updateUser(userId, request, actor, roles);
    }

    @Operation(summary = "사용자 물리 삭제")
    @PostMapping("/{userId}/delete")
    public ApiResponse<Void> deleteUser(
            @PathVariable String userId,
            @RequestHeader(value = InternalAuthHeaders.USER, required = false) String actor,
            @RequestHeader(value = InternalAuthHeaders.ROLES, required = false) String roles
    ) {
        // 삭제 대상과 Gateway가 보증한 작업자·역할 정보를 서비스에 전달해 권한 검증을 유지한다.
        return userManagementService.deleteUser(userId, actor, roles);
    }
}
