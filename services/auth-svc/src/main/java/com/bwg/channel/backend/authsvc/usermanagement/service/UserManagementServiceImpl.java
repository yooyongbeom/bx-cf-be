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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        // 비밀번호는 nonblank 여부만 trim으로 확인하고 저장할 원문은 변경하지 않는다.
        BusinessValidator.requireNonBlank(data.getUsrPwd(), "usrPwd");

        if (userManagementRepository.existsUser(data.getUsrId())) {
            throw duplicateUser(data.getUsrId());
        }

        boolean sessionCreationBlocked;
        try {
            // 영구 tombstone이 남은 물리 삭제 ID는 신규 계정으로 다시 사용할 수 없다.
            sessionCreationBlocked = sessionContextService.isSessionCreationBlocked(data.getUsrId());
        } catch (RuntimeException exception) {
            // Redis 접근/직렬화 실패만 원인을 보존한 서비스 불가 오류로 변환한다.
            throw translateSessionStoreFailure(exception, "Unable to verify deleted user ID");
        }
        if (sessionCreationBlocked) {
            throw deletedUserIdReuse(data.getUsrId());
        }

        // 역할 ID를 하드코딩하지 않고 ROLE_USER가 DB에 정확히 하나 존재하는지 확인한다.
        List<Long> roleIds = userManagementRepository.findRoleIdsByName(DEFAULT_ROLE_NAME);
        if (roleIds == null || roleIds.size() != 1) {
            throw databaseSaveFailure("defaultRole");
        }

        try {
            // 사용자 본문을 먼저 만들고, 같은 트랜잭션에서 기본 역할 하나만 연결한다.
            requireSingleAffectedRow(userManagementRepository.insertUser(request, createdBy), "insertUser");
            // TODO 사용자 역할 관리 기능이 추가되면 요청 역할 목록을 검증하여 USER_ROLES 연결을 관리한다.
            requireSingleAffectedRow(
                    userManagementRepository.insertUserRole(data.getUsrId(), roleIds.get(0)),
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

        requireSingleAffectedRow(
                userManagementRepository.updateUser(requiredUserId, request, updatedBy),
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

        // 요청마다 고유한 토큰을 만들어 이 삭제 작업이 획득한 tombstone만 보상할 수 있게 한다.
        String operationId = UUID.randomUUID().toString();
        boolean blockAcquired;
        try {
            // SET NX 획득 결과가 true인 작업만 이후 세션ㆍDB 삭제를 수행한다.
            blockAcquired = sessionContextService.blockSessionCreation(requiredUserId, operationId);
        } catch (RuntimeException exception) {
            // 획득 여부를 확정하지 못한 작업은 소유권이 없으므로 다른 작업의 marker를 보상하지 않는다.
            throw translateSessionStoreFailure(exception, "Unable to block user sessions");
        }
        if (!blockAcquired) {
            // 다른 작업이 marker를 소유하면 변경 없이 명시적인 업무 충돌로 호출자에게 알린다.
            throw deleteAlreadyInProgress(requiredUserId);
        }

        try {
            // 메서드 반환 뒤 트랜잭션이 롤백되는 경우에도 소유한 marker를 compare-delete로 보상한다.
            registerTombstoneRollbackCompensation(requiredUserId, operationId);
            sessionContextService.deleteByUserId(requiredUserId);
        } catch (RuntimeException exception) {
            // 직접 호출에서도 안전하도록 반환 전 실패는 동일 소유자 토큰으로 즉시 보상한다.
            bestEffortUnblockSessionCreation(requiredUserId, operationId);
            throw translateSessionStoreFailure(exception, "Unable to revoke user sessions");
        }

        try {
            // 세션 폐기가 끝난 뒤 FK 참조를 제거하고 사용자 본문을 물리 삭제한다.
            userManagementRepository.deleteUserRoles(requiredUserId);
            requireSingleAffectedRow(userManagementRepository.deleteUser(requiredUserId), "deleteUser");
        } catch (RuntimeException exception) {
            // 관계형 삭제가 실패하면 동일 작업이 소유한 tombstone만 즉시 보상한다.
            bestEffortUnblockSessionCreation(requiredUserId, operationId);
            throw exception;
        }
        // 물리 삭제 성공 후에는 동일 ID의 세션이 다시 발급되지 않도록 tombstone을 유지한다.
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

    private BwgBusinessException deletedUserIdReuse(String userId) {
        // 삭제 tombstone은 과거 in-flight 인증을 계속 거부하므로 관리 API에서 ID 재사용을 금지한다.
        return new BwgBusinessException.Builder()
                .code(BusinessErrorCode.BUSINESS_RULE_VIOLATION)
                .message(BusinessErrorCode.BUSINESS_RULE_VIOLATION.getMsg())
                .details(Map.of("userId", userId, "reason", "deletedUserIdNotReusable"))
                .build();
    }

    private BwgBusinessException deleteAlreadyInProgress(String userId) {
        // marker 소유권 충돌은 재시도 판단에 필요한 안전한 업무 사유만 노출한다.
        return new BwgBusinessException.Builder()
                .code(BusinessErrorCode.BUSINESS_RULE_VIOLATION)
                .message(BusinessErrorCode.BUSINESS_RULE_VIOLATION.getMsg())
                .details(Map.of("userId", userId, "reason", "deleteAlreadyInProgress"))
                .build();
    }

    private void requireSingleAffectedRow(int affectedRows, String operation) {
        // 사용자 변경 SQL은 정확히 한 건만 반영되어야 하며, 그렇지 않으면 저장 실패로 처리한다.
        if (affectedRows != 1) {
            throw databaseSaveFailure(operation);
        }
    }

    private BwgAuthException databaseSaveFailure(String operation) {
        // 저장 오류의 세부 원인은 노출하지 않고, 민감하지 않은 작업 이름만 오류 상세 정보에 남긴다.
        return new BwgAuthException.Builder()
                .code(CommonErrorCode.DB_SAVE_DATA_ERROR)
                .message("Unable to save user data")
                .details(Map.of("operation", operation))
                .build();
    }

    private RuntimeException translateSessionStoreFailure(RuntimeException exception, String message) {
        // Redis 계층이 명시한 접근/직렬화 실패만 외부 노출이 안전한 서비스 불가 오류로 변환한다.
        if (exception instanceof DataAccessException || exception instanceof SerializationException) {
            return new BwgAuthException.Builder()
                    .code(CommonErrorCode.SERVICE_UNAVAILABLE)
                    .message(message)
                    .cause(exception)
                    .build();
        }
        return exception;
    }

    private void registerTombstoneRollbackCompensation(String userId, String operationId) {
        // 트랜잭션 commit 이후에는 durable tombstone을 유지하고 그 외 종료 상태만 보상한다.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    bestEffortUnblockSessionCreation(userId, operationId);
                }
            }
        });
    }

    private void bestEffortUnblockSessionCreation(String userId, String operationId) {
        try {
            // 최초 실패를 가리지 않으면서 이 작업이 소유한 tombstone만 compare-delete로 제거한다.
            sessionContextService.unblockSessionCreation(userId, operationId);
        } catch (RuntimeException ignored) {
            // 보상 실패보다 최초 세션/DB 실패 원인을 우선해 호출자에게 전달한다.
        }
    }
}
