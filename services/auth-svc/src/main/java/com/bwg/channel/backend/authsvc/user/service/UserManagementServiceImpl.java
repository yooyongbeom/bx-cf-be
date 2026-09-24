package com.bwg.channel.backend.authsvc.user.service;

import com.bwg.channel.backend.authsvc.user.dto.UserCreateReqDto;
import com.bwg.channel.backend.authsvc.user.dto.UserDetailResDto;
import com.bwg.channel.backend.authsvc.user.dto.UserListResDto;
import com.bwg.channel.backend.authsvc.user.dto.UserUpdateReqDto;
import com.bwg.channel.backend.authsvc.user.repository.UserManagementRepository;
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

/** 사용자 관리 관련 처리 서비스 구현 */
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";
    private static final String DEFAULT_ROLE_NAME = "ROLE_USER";

    private final UserManagementRepository userManagementRepository;
    private final SessionContextService sessionContextService;

    /**
     * 관리자 권한을 검증한 뒤 전체 사용자 목록 반환
     *
     * @param roles Gateway가 전달한 콤마(,) 구분 역할 목록
     * @return 전체 사용자 목록과 페이지 정보가 포함된 응답
     * @throws BwgAuthException 관리자 권한이 없는 경우
     */
    @Override
    public ApiResponse<List<UserListResDto>> getUsers(String roles) {
        // 목록을 조회하기 전에 관리자 역할 확인 후 일반 사용자의 계정 정보 접근을 차단한다.
        requireAdmin(roles);
        List<UserListResDto> users = userManagementRepository.findUsers();
        return ApiResponse.success(users, PageUtil.singlePage(users));
    }

    /**
     * 관리자 권한과 사용자 ID를 검증 후 사용자 상세정보 조회
     *
     * @param userId 조회할 사용자 ID
     * @param roles Gateway가 전달한 콤마 구분 역할 목록
     * @return 사용자 상세정보가 포함된 응답
     * @throws BwgAuthException 관리자 권한이 없는 경우
     * @throws BwgBusinessException 사용자 ID가 없거나 대상 사용자가 존재하지 않는 경우
     */
    @Override
    public ApiResponse<UserDetailResDto> getUser(String userId, String roles) {
        // 관리자 역할 확인
        requireAdmin(roles);

        // 사용자 ID 검증 후 존재하면 상세 정보 반환
        String requiredUserId = BusinessValidator.requireNonBlank(userId, "userId");
        UserDetailResDto user = BusinessValidator.requireFound(
                userManagementRepository.findUser(requiredUserId),
                "user"
        );
        return ApiResponse.success(user);
    }

    /**
     * 등록값과 삭제 사용자 ID 재사용 여부를 검증하고 사용자와 기본 역할을 동일 트랜잭션에서 등록한다.
     *
     * <p>사전 중복 검사 이후의 동시 등록으로 발생한 DB 고유키 위반도 동일한 사용자 ID 중복
     * 업무 오류로 변환한다.</p>
     *
     * @param request 등록할 사용자 정보
     * @param actor Gateway가 검증한 작업자 ID
     * @param roles Gateway가 전달한 쉼표 구분 역할 목록
     * @return 등록 성공 응답
     * @throws BwgAuthException 관리자 권한이 없거나 사용자 또는 세션 저장소 처리에 실패한 경우
     * @throws BwgBusinessException 필수값이 없거나 사용자 ID가 중복되거나 재사용할 수 없는 경우
     */
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
                    userManagementRepository.insertUserRole(data.getUsrId(), roleIds.get(0), createdBy),
                    "insertUserRole"
            );
        } catch (DuplicateKeyException exception) {
            // 사전 존재 확인 뒤 동시 등록이 일어나도 사용자 ID 중복이라는 동일한 업무 오류로 응답한다.
            throw duplicateUser(data.getUsrId());
        }

        return ApiResponse.success(null);
    }

    /**
     * 관리자 권한과 수정 대상을 검증하고 사용자 기본정보를 수정한다.
     *
     * <p>비밀번호와 사용자 역할은 수정하지 않으며, 정확히 한 건이 반영되지 않으면 저장 실패로 처리한다.</p>
     *
     * @param userId 수정할 사용자 ID
     * @param request 수정할 사용자 기본정보
     * @param actor Gateway가 검증한 작업자 ID
     * @param roles Gateway가 전달한 쉼표 구분 역할 목록
     * @return 수정 성공 응답
     * @throws BwgAuthException 관리자 권한이 없거나 사용자 저장에 실패한 경우
     * @throws BwgBusinessException 필수값이 없거나 대상 사용자가 존재하지 않는 경우
     */
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

    /**
     * 사용자 신규 세션 생성을 차단하고 기존 세션과 역할 연결을 제거한 뒤 사용자를 물리 삭제한다.
     *
     * <p>Redis tombstone은 작업별 소유권 토큰으로 획득한다. 세션 또는 DB 삭제가 실패하거나
     * 트랜잭션이 롤백되면 동일 토큰을 소유한 tombstone만 보상 삭제하고, 삭제가 커밋되면
     * 진행 중이던 인증 요청과 삭제 사용자 ID 재사용을 막기 위해 tombstone을 유지한다.</p>
     *
     * @param userId 삭제할 사용자 ID
     * @param actor Gateway가 검증한 작업자 ID
     * @param roles Gateway가 전달한 쉼표 구분 역할 목록
     * @return 삭제 성공 응답
     * @throws BwgAuthException 관리자 권한이 없거나 세션 또는 사용자 저장소 처리에 실패한 경우
     * @throws BwgBusinessException 대상 사용자가 없거나 다른 삭제 작업이 진행 중인 경우
     */
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
            // SET NX가 적용된 뒤 응답만 유실됐을 수 있어 시도한 동일 토큰으로만 best-effort 보상한다.
            bestEffortUnblockSessionCreation(requiredUserId, operationId);
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

    /**
     * Gateway가 전달한 역할 목록에 관리자 역할이 있는지 확인한다.
     *
     * @param roles Gateway가 전달한 콤마 구분 역할 목록
     * @throws BwgAuthException 역할 목록에 {@code ROLE_ADMIN}이 없는 경우
     */
    private void requireAdmin(String roles) {
        // ROLE_ADMIN이 포함된 경우에만 관리기능 허용
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

    /**
     * 시스템 변경 사용자 필드에 사용할 Gateway 검증 작업자 ID를 확인한다.
     *
     * @param actor Gateway가 검증한 작업자 ID
     * @return 공백을 제거한 작업자 ID
     * @throws BwgBusinessException 작업자 ID가 없는 경우
     */
    private String requireTrustedActor(String actor) {
        // 클라이언트 입력이 아닌 검증된 JWT subject만 시스템 변경 사용자 필드에 사용한다.
        return BusinessValidator.requireNonBlank(actor, "actor");
    }

    /**
     * 수정 또는 삭제할 사용자가 현재 저장소에 존재하는지 확인한다.
     *
     * @param userId 확인할 사용자 ID
     * @throws BwgBusinessException 대상 사용자가 존재하지 않는 경우
     */
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

    /**
     * 사용자 ID 고유성 위반을 나타내는 표준 업무 예외를 생성한다.
     *
     * @param userId 중복된 사용자 ID
     * @return 중복 사용자 ID를 상세정보로 포함한 업무 예외
     */
    private BwgBusinessException duplicateUser(String userId) {
        // 사전 검증과 DB 고유키 위반이 동일한 업무 오류 계약을 사용하도록 예외를 정규화한다.
        return new BwgBusinessException.Builder()
                .code(BusinessErrorCode.BUSINESS_RULE_VIOLATION)
                .message(BusinessErrorCode.BUSINESS_RULE_VIOLATION.getMsg())
                .details(Map.of("userId", userId))
                .build();
    }

    /**
     * 물리 삭제된 사용자 ID를 다시 사용할 수 없음을 나타내는 표준 업무 예외를 생성한다.
     *
     * @param userId 재사용을 요청한 삭제 사용자 ID
     * @return 사용자 ID 재사용 금지 사유를 포함한 업무 예외
     */
    private BwgBusinessException deletedUserIdReuse(String userId) {
        // 삭제 tombstone은 과거 in-flight 인증을 계속 거부하므로 관리 API에서 ID 재사용을 금지한다.
        return new BwgBusinessException.Builder()
                .code(BusinessErrorCode.BUSINESS_RULE_VIOLATION)
                .message(BusinessErrorCode.BUSINESS_RULE_VIOLATION.getMsg())
                .details(Map.of("userId", userId, "reason", "deletedUserIdNotReusable"))
                .build();
    }

    /**
     * 다른 요청이 동일 사용자의 삭제 tombstone을 소유하고 있음을 나타내는 업무 예외를 생성한다.
     *
     * @param userId 삭제가 진행 중인 사용자 ID
     * @return 삭제 작업 충돌 사유를 포함한 업무 예외
     */
    private BwgBusinessException deleteAlreadyInProgress(String userId) {
        // marker 소유권 충돌은 재시도 판단에 필요한 안전한 업무 사유만 노출한다.
        return new BwgBusinessException.Builder()
                .code(BusinessErrorCode.BUSINESS_RULE_VIOLATION)
                .message(BusinessErrorCode.BUSINESS_RULE_VIOLATION.getMsg())
                .details(Map.of("userId", userId, "reason", "deleteAlreadyInProgress"))
                .build();
    }

    /**
     * 사용자 변경 SQL이 정확히 한 건을 반영했는지 확인한다.
     *
     * @param affectedRows SQL 반영 건수
     * @param operation 오류 상세정보에 기록할 작업 이름
     * @throws BwgAuthException 반영 건수가 한 건이 아닌 경우
     */
    private void requireSingleAffectedRow(int affectedRows, String operation) {
        // 사용자 변경 SQL은 정확히 한 건만 반영되어야 하며, 그렇지 않으면 저장 실패로 처리한다.
        if (affectedRows != 1) {
            throw databaseSaveFailure(operation);
        }
    }

    /**
     * 사용자 저장 실패의 내부 정보를 노출하지 않는 표준 인증 예외를 생성한다.
     *
     * @param operation 실패한 저장 작업 이름
     * @return 작업 이름만 상세정보로 포함한 저장 실패 예외
     */
    private BwgAuthException databaseSaveFailure(String operation) {
        // 저장 오류의 세부 원인은 노출하지 않고, 민감하지 않은 작업 이름만 오류 상세 정보에 남긴다.
        return new BwgAuthException.Builder()
                .code(CommonErrorCode.DB_SAVE_DATA_ERROR)
                .message("Unable to save user data")
                .details(Map.of("operation", operation))
                .build();
    }

    /**
     * 외부 노출이 안전한 Redis 접근ㆍ직렬화 실패를 표준 서비스 불가 예외로 변환한다.
     *
     * @param exception Redis 처리 중 발생한 예외
     * @param message 호출자에게 전달할 안전한 오류 메시지
     * @return 알려진 저장소 예외는 표준 인증 예외, 그 외에는 원래 예외
     */
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

    /**
     * 사용자 삭제 트랜잭션이 커밋되지 않으면 이 작업이 소유한 tombstone을 제거하도록 보상을 등록한다.
     *
     * @param userId 삭제 대상 사용자 ID
     * @param operationId tombstone 소유권을 확인할 삭제 작업 ID
     */
    private void registerTombstoneRollbackCompensation(String userId, String operationId) {
        // 트랜잭션 commit 이후에는 durable tombstone을 유지하고 그 외 종료 상태만 보상한다.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * 삭제 트랜잭션이 커밋되지 않았으면 소유권 토큰으로 tombstone 제거를 시도한다.
             *
             * @param status Spring 트랜잭션 완료 상태
             */
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    bestEffortUnblockSessionCreation(userId, operationId);
                }
            }
        });
    }

    /**
     * 최초 실패 원인을 유지하면서 이 작업이 소유한 세션 생성 차단 tombstone 제거를 시도한다.
     *
     * @param userId 세션 생성 차단을 해제할 사용자 ID
     * @param operationId tombstone 소유권을 확인할 삭제 작업 ID
     */
    private void bestEffortUnblockSessionCreation(String userId, String operationId) {
        try {
            // 최초 실패를 가리지 않으면서 이 작업이 소유한 tombstone만 compare-delete로 제거한다.
            sessionContextService.unblockSessionCreation(userId, operationId);
        } catch (RuntimeException ignored) {
            // 보상 실패보다 최초 세션/DB 실패 원인을 우선해 호출자에게 전달한다.
        }
    }
}
