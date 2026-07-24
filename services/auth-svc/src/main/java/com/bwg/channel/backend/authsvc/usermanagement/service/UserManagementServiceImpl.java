package com.bwg.channel.backend.authsvc.usermanagement.service;

import com.bwg.channel.backend.authsvc.usermanagement.dto.UserCreateReqDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserDetailResDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserListResDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserUpdateReqDto;
import com.bwg.channel.backend.authsvc.usermanagement.repository.UserManagementRepository;
import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import com.bwg.channel.backend.businesscommon.validation.BusinessValidator;
import com.bwg.channel.backend.common.constants.error.CommonErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.util.PageUtil;
import com.bwg.channel.backend.securitycommon.constants.AuthErrorCode;
import com.bwg.channel.backend.securitycommon.exception.BwgAuthException;
import com.bwg.channel.backend.sessioncontext.service.SessionContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** 사용자 관리 권한, 입력값, 사용자ㆍ역할 데이터와 세션 정합성을 처리하는 서비스 구현체. */
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";
    private static final String DEFAULT_ROLE_NAME = "ROLE_USER";

    private final UserManagementRepository userManagementRepository;
    private final SessionContextService sessionContextService;

    @Override
    public ApiResponse<List<UserListResDto>> getUsers(String roles) {
        // 목록을 조회하기 전에 관리자 역할을 확인하여 일반 사용자의 계정 정보 접근을 차단한다.
        requireAdmin(roles);
        List<UserListResDto> users = userManagementRepository.findUsers();
        return ApiResponse.success(users, PageUtil.singlePage(users));
    }

    @Override
    public ApiResponse<UserDetailResDto> getUser(String userId, String roles) {
        // 관리자 역할과 대상 사용자 ID를 검증한 뒤 존재하는 상세 정보만 반환한다.
        requireAdmin(roles);
        String requiredUserId = BusinessValidator.requireNonBlank(userId, "userId");
        UserDetailResDto user = BusinessValidator.requireFound(
                userManagementRepository.findUser(requiredUserId),
                "user"
        );
        return ApiResponse.success(user);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> createUser(
            ApiRequest<UserCreateReqDto> request,
            String actor,
            String roles
    ) {
        // 권한ㆍ신뢰된 작업자ㆍ필수 등록값을 DB 변경 전에 모두 확정한다.
        requireAdmin(roles);
        String createdBy = requireTrustedActor(actor);
        UserCreateReqDto data = BusinessValidator.requireData(request);
        data.setUsrId(BusinessValidator.requireNonBlank(data.getUsrId(), "usrId"));
        data.setUsrNm(BusinessValidator.requireNonBlank(data.getUsrNm(), "usrNm"));
        data.setUsrPwd(BusinessValidator.requireNonBlank(data.getUsrPwd(), "usrPwd"));

        if (userManagementRepository.existsUser(data.getUsrId())) {
            throw duplicateUser(data.getUsrId());
        }

        // 역할 ID를 하드코딩하지 않고 ROLE_USER가 DB에 정확히 하나 존재하는지 확인한다.
        List<Long> roleIds = userManagementRepository.findRoleIdsByName(DEFAULT_ROLE_NAME);
        if (roleIds == null || roleIds.size() != 1) {
            throw databaseSaveFailure("Default role must exist exactly once: " + DEFAULT_ROLE_NAME);
        }

        try {
            // 사용자 본문을 먼저 만들고, 같은 트랜잭션에서 기본 역할 하나만 연결한다.
            BusinessValidator.requireAffectedRows(
                    userManagementRepository.insertUser(request, createdBy),
                    1,
                    "insertUser"
            );
            // TODO 사용자 역할 관리 기능이 추가되면 요청 역할 목록을 검증하여 USER_ROLES 연결을 관리한다.
            BusinessValidator.requireAffectedRows(
                    userManagementRepository.insertUserRole(data.getUsrId(), roleIds.get(0), createdBy),
                    1,
                    "insertUserRole"
            );
        } catch (DuplicateKeyException exception) {
            // 사전 존재 확인 뒤 동시 등록이 일어나도 사용자 ID 중복이라는 동일한 업무 오류로 응답한다.
            throw duplicateUser(data.getUsrId());
        }
        return ApiResponse.success(null);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateUser(
            String userId,
            ApiRequest<UserUpdateReqDto> request,
            String actor,
            String roles
    ) {
        // 수정은 사용자 기본 정보만 대상으로 하며 비밀번호와 USER_ROLES는 절대 변경하지 않는다.
        requireAdmin(roles);
        String updatedBy = requireTrustedActor(actor);
        String requiredUserId = BusinessValidator.requireNonBlank(userId, "userId");
        UserUpdateReqDto data = BusinessValidator.requireData(request);
        data.setUsrNm(BusinessValidator.requireNonBlank(data.getUsrNm(), "usrNm"));
        requireExistingUser(requiredUserId);

        BusinessValidator.requireAffectedRows(
                userManagementRepository.updateUser(requiredUserId, request, updatedBy),
                1,
                "updateUser"
        );
        return ApiResponse.success(null);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> deleteUser(String userId, String actor, String roles) {
        // DB 및 세션을 변경하기 전에 관리자 권한, 신뢰된 작업자, 삭제 대상을 모두 검증한다.
        requireAdmin(roles);
        requireTrustedActor(actor);
        String requiredUserId = BusinessValidator.requireNonBlank(userId, "userId");
        requireExistingUser(requiredUserId);

        try {
            // DB 삭제 전에 대상 사용자의 모든 세션을 폐기하여 삭제 진행 중에도 재접속을 차단한다.
            sessionContextService.deleteByUserId(requiredUserId);
        } catch (RuntimeException exception) {
            // 세션 폐기에 실패하면 DB 삭제를 시작하지 않고 안전한 서비스 오류로 변환한다.
            throw new BwgAuthException.Builder()
                    .code(CommonErrorCode.SERVICE_UNAVAILABLE)
                    .message("Unable to revoke user sessions")
                    .build();
        }

        // 세션 폐기가 끝난 뒤 FK 참조를 제거하고 사용자 본문을 물리 삭제한다.
        userManagementRepository.deleteUserRoles(requiredUserId);
        BusinessValidator.requireAffectedRows(
                userManagementRepository.deleteUser(requiredUserId),
                1,
                "deleteUser"
        );
        return ApiResponse.success(null);
    }

    private void requireAdmin(String roles) {
        // 쉼표 구분 역할 문자열에서 ROLE_ADMIN이 정확히 포함된 경우에만 관리 기능을 허용한다.
        boolean isAdmin = roles != null && Arrays.stream(roles.split(","))
                .map(String::trim)
                .anyMatch(ADMIN_ROLE_NAME::equals);
        if (!isAdmin) {
            throw new BwgAuthException.Builder()
                    .code(AuthErrorCode.ACCESS_DENIED)
                    .message(AuthErrorCode.ACCESS_DENIED.getMsg())
                    .build();
        }
    }

    private String requireTrustedActor(String actor) {
        // Gateway가 검증된 JWT subject로 주입한 작업자 ID만 감사 필드에 사용한다.
        return BusinessValidator.requireNonBlank(actor, "actor");
    }

    private void requireExistingUser(String userId) {
        // 수정ㆍ삭제 대상이 없으면 변경 SQL을 실행하지 않고 표준 미존재 오류를 반환한다.
        if (!userManagementRepository.existsUser(userId)) {
            throw new BwgBusinessException.Builder()
                    .code(BusinessErrorCode.BUSINESS_DATA_NOT_FOUND)
                    .message(BusinessErrorCode.BUSINESS_DATA_NOT_FOUND.getMsg())
                    .details(Map.of("target", "user", "userId", userId))
                    .build();
        }
    }

    private BwgBusinessException duplicateUser(String userId) {
        // 사용자 ID 고유성 위반은 사전 검증과 DB 제약 위반 모두 같은 업무 오류로 정규화한다.
        return new BwgBusinessException.Builder()
                .code(BusinessErrorCode.BUSINESS_RULE_VIOLATION)
                .message(BusinessErrorCode.BUSINESS_RULE_VIOLATION.getMsg())
                .details(Map.of("userId", userId))
                .build();
    }

    private BwgAuthException databaseSaveFailure(String message) {
        // 기본 역할 데이터의 이상은 인증ㆍ권한 구성이 깨진 서버 저장 오류로 처리한다.
        return new BwgAuthException.Builder()
                .code(CommonErrorCode.DB_SAVE_DATA_ERROR)
                .message(message)
                .build();
    }
}
