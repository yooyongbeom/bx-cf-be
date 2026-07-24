package com.bwg.channel.backend.authsvc.user.service;

import com.bwg.channel.backend.authsvc.user.dto.UserCreateReqDto;
import com.bwg.channel.backend.authsvc.user.dto.UserDetailResDto;
import com.bwg.channel.backend.authsvc.user.dto.UserListResDto;
import com.bwg.channel.backend.authsvc.user.dto.UserUpdateReqDto;
import com.bwg.channel.backend.authsvc.user.repository.UserManagementRepository;
import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import com.bwg.channel.backend.common.constants.error.CommonErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.securitycommon.constants.AuthErrorCode;
import com.bwg.channel.backend.securitycommon.exception.BwgAuthException;
import com.bwg.channel.backend.sessioncontext.service.SessionContextService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserManagementServiceTests {

    private final UserManagementRepository repository = mock(UserManagementRepository.class);
    private final SessionContextService sessionContextService = mock(SessionContextService.class);
    private final UserManagementService service =
            new UserManagementServiceImpl(repository, sessionContextService);

    @BeforeEach
    void initializeTransactionSynchronization() {
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void rejectsNonAdminBeforeRepositoryAccess() {
        assertThatThrownBy(() -> service.getUsers("ROLE_USER"))
                .isInstanceOf(BwgAuthException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.ACCESS_DENIED);

        verify(repository, never()).findUsers();
    }

    @Test
    void rejectsMissingRolesThroughExplicitServiceAuthorization() {
        assertThatThrownBy(() -> service.getUsers(null))
                .isInstanceOf(BwgAuthException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.ACCESS_DENIED);

        verify(repository, never()).findUsers();
    }

    @Test
    void rejectsMissingActorThroughExplicitServiceValidation() {
        assertThatThrownBy(() -> service.createUser(new ApiRequest<>(), null, "ROLE_ADMIN"))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.REQUIRED_VALUE_MISSING);

        verify(repository, never()).existsUser(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void returnsUsersWithSinglePageMetadata() {
        UserListResDto user = new UserListResDto();
        user.setUsrId("hong.gildong");
        when(repository.findUsers()).thenReturn(List.of(user));

        ApiResponse<List<UserListResDto>> response = service.getUsers("ROLE_ADMIN,ROLE_USER");

        assertThat(response.getPayload()).extracting(UserListResDto::getUsrId)
                .containsExactly("hong.gildong");
        assertThat(response.getPagination().getTotalCount()).isEqualTo(1L);
    }

    @Test
    void returnsUserDetailWithoutPagination() {
        UserDetailResDto user = new UserDetailResDto();
        user.setUsrId("hong.gildong");
        when(repository.findUser("hong.gildong")).thenReturn(user);

        ApiResponse<UserDetailResDto> response = service.getUser(" hong.gildong ", "ROLE_ADMIN");

        assertThat(response.getPayload().getUsrId()).isEqualTo("hong.gildong");
        assertThat(response.getPagination()).isNull();
    }

    @Test
    void createsUserThenExactlyOneDefaultRole() {
        UserCreateReqDto data = new UserCreateReqDto();
        data.setUsrId(" new.user ");
        data.setUsrNm("신규 사용자");
        data.setUsrPwd("  password with spaces  ");
        ApiRequest<UserCreateReqDto> request = new ApiRequest<>();
        request.setData(data);

        when(sessionContextService.isSessionCreationBlocked("new.user")).thenReturn(false);
        when(repository.findRoleIdsByName("ROLE_USER")).thenReturn(List.of(7L));
        when(repository.insertUser(request, "admin")).thenReturn(1);
        when(repository.insertUserRole("new.user", 7L)).thenReturn(1);

        service.createUser(request, " admin ", "ROLE_ADMIN");

        assertThat(data.getUsrId()).isEqualTo("new.user");
        assertThat(data.getUsrNm()).isEqualTo("신규 사용자");
        // 비밀번호는 nonblank 검증에만 trim을 사용하고 실제 저장 요청 값은 그대로 보존한다.
        assertThat(data.getUsrPwd()).isEqualTo("  password with spaces  ");
        InOrder order = inOrder(repository, sessionContextService);
        order.verify(repository).existsUser("new.user");
        order.verify(sessionContextService).isSessionCreationBlocked("new.user");
        order.verify(repository).findRoleIdsByName("ROLE_USER");
        order.verify(repository).insertUser(request, "admin");
        order.verify(repository).insertUserRole("new.user", 7L);
        verify(sessionContextService, never()).unblockSessionCreation(eq("new.user"), anyString());
    }

    @Test
    void rejectsDuplicateUserBeforeDefaultRoleLookup() {
        UserCreateReqDto data = new UserCreateReqDto();
        data.setUsrId("existing");
        data.setUsrNm("기존 사용자");
        data.setUsrPwd("password");
        ApiRequest<UserCreateReqDto> request = new ApiRequest<>();
        request.setData(data);
        when(repository.existsUser("existing")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(request, "admin", "ROLE_ADMIN"))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.BUSINESS_RULE_VIOLATION);

        verify(repository, never()).findRoleIdsByName("ROLE_USER");
    }

    @Test
    void rejectsMissingOrDuplicateDefaultRole() {
        UserCreateReqDto data = new UserCreateReqDto();
        data.setUsrId("new.user");
        data.setUsrNm("신규 사용자");
        data.setUsrPwd("password");
        ApiRequest<UserCreateReqDto> request = new ApiRequest<>();
        request.setData(data);

        when(repository.findRoleIdsByName("ROLE_USER")).thenReturn(List.of());
        assertThatThrownBy(() -> service.createUser(request, "admin", "ROLE_ADMIN"))
                .isInstanceOf(BwgAuthException.class)
                .extracting("code")
                .isEqualTo(CommonErrorCode.DB_SAVE_DATA_ERROR);

        when(repository.findRoleIdsByName("ROLE_USER")).thenReturn(List.of(1L, 2L));
        assertThatThrownBy(() -> service.createUser(request, "admin", "ROLE_ADMIN"))
                .isInstanceOf(BwgAuthException.class)
                .extracting("code")
                .isEqualTo(CommonErrorCode.DB_SAVE_DATA_ERROR);
    }

    @Test
    void rejectsUserCreationWhenInsertAffectsUnexpectedRows() {
        UserCreateReqDto data = new UserCreateReqDto();
        data.setUsrId("new.user");
        data.setUsrNm("신규 사용자");
        data.setUsrPwd("password");
        ApiRequest<UserCreateReqDto> request = new ApiRequest<>();
        request.setData(data);
        when(repository.findRoleIdsByName("ROLE_USER")).thenReturn(List.of(7L));
        when(repository.insertUser(request, "admin")).thenReturn(0);

        assertThatThrownBy(() -> service.createUser(request, "admin", "ROLE_ADMIN"))
                .isInstanceOf(BwgAuthException.class)
                .extracting("code")
                .isEqualTo(CommonErrorCode.DB_SAVE_DATA_ERROR);

        verify(repository, never()).insertUserRole("new.user", 7L);
        verify(sessionContextService, never()).unblockSessionCreation(eq("new.user"), anyString());
    }

    @Test
    void rejectsDurablyDeletedUserIdWithoutClearingItsTombstoneOrInsertingRows() {
        UserCreateReqDto data = new UserCreateReqDto();
        data.setUsrId("new.user");
        data.setUsrNm("신규 사용자");
        data.setUsrPwd("password");
        ApiRequest<UserCreateReqDto> request = new ApiRequest<>();
        request.setData(data);
        when(sessionContextService.isSessionCreationBlocked("new.user")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(request, "admin", "ROLE_ADMIN"))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.BUSINESS_RULE_VIOLATION);

        verify(repository, never()).findRoleIdsByName("ROLE_USER");
        verify(repository, never()).insertUser(request, "admin");
        verify(repository, never()).insertUserRole(eq("new.user"), org.mockito.ArgumentMatchers.anyLong());
        verify(sessionContextService, never()).unblockSessionCreation(eq("new.user"), anyString());
    }

    @Test
    void translatesRedisAccessFailureDuringDeletedIdCheckAndPreservesCause() {
        ApiRequest<UserCreateReqDto> request = createRequest("new.user");
        RedisConnectionFailureException redisFailure =
                new RedisConnectionFailureException("Redis is unavailable");
        doThrow(redisFailure).when(sessionContextService).isSessionCreationBlocked("new.user");

        Throwable thrown = catchThrowable(() -> service.createUser(request, "admin", "ROLE_ADMIN"));

        assertThat(thrown).isInstanceOf(BwgAuthException.class).hasCause(redisFailure);
        assertThat(((BwgAuthException) thrown).getCode()).isEqualTo(CommonErrorCode.SERVICE_UNAVAILABLE);
        verify(repository, never()).findRoleIdsByName("ROLE_USER");
    }

    @Test
    void translatesRedisSerializationFailureDuringDeletedIdCheckAndPreservesCause() {
        ApiRequest<UserCreateReqDto> request = createRequest("new.user");
        SerializationException redisFailure = new SerializationException("Redis response cannot be decoded");
        doThrow(redisFailure).when(sessionContextService).isSessionCreationBlocked("new.user");

        Throwable thrown = catchThrowable(() -> service.createUser(request, "admin", "ROLE_ADMIN"));

        assertThat(thrown).isInstanceOf(BwgAuthException.class).hasCause(redisFailure);
        assertThat(((BwgAuthException) thrown).getCode()).isEqualTo(CommonErrorCode.SERVICE_UNAVAILABLE);
        verify(repository, never()).findRoleIdsByName("ROLE_USER");
    }

    @Test
    void preservesUnexpectedDeletedIdCheckFailureType() {
        ApiRequest<UserCreateReqDto> request = createRequest("new.user");
        IllegalStateException unexpected = new IllegalStateException("Unexpected session-store failure");
        doThrow(unexpected).when(sessionContextService).isSessionCreationBlocked("new.user");

        assertThatThrownBy(() -> service.createUser(request, "admin", "ROLE_ADMIN"))
                .isSameAs(unexpected);

        verify(repository, never()).findRoleIdsByName("ROLE_USER");
    }

    @Test
    void updatesOnlyUserBasicFieldsThroughRepository() {
        UserUpdateReqDto data = new UserUpdateReqDto();
        data.setUsrNm(" 변경 사용자 ");
        ApiRequest<UserUpdateReqDto> request = new ApiRequest<>();
        request.setData(data);
        when(repository.existsUser("target")).thenReturn(true);
        when(repository.updateUser("target", request, "admin")).thenReturn(1);

        service.updateUser("target", request, "admin", "ROLE_ADMIN");

        assertThat(data.getUsrNm()).isEqualTo("변경 사용자");
        verify(repository).updateUser("target", request, "admin");
        verify(repository, never()).deleteUserRoles("target");
    }

    @Test
    void rejectsUserUpdateWhenAffectedRowsAreUnexpected() {
        UserUpdateReqDto data = new UserUpdateReqDto();
        data.setUsrNm("변경 사용자");
        ApiRequest<UserUpdateReqDto> request = new ApiRequest<>();
        request.setData(data);
        when(repository.existsUser("target")).thenReturn(true);
        when(repository.updateUser("target", request, "admin")).thenReturn(0);

        assertThatThrownBy(() -> service.updateUser("target", request, "admin", "ROLE_ADMIN"))
                .isInstanceOf(BwgAuthException.class)
                .extracting("code")
                .isEqualTo(CommonErrorCode.DB_SAVE_DATA_ERROR);
    }

    @Test
    void blocksAndRevokesTargetSessionsBeforeDeletingRolesAndUser() {
        when(repository.existsUser("target")).thenReturn(true);
        when(sessionContextService.blockSessionCreation(eq("target"), anyString())).thenReturn(true);
        when(repository.deleteUser("target")).thenReturn(1);

        service.deleteUser("target", "admin", "ROLE_ADMIN");

        ArgumentCaptor<String> operationIdCaptor = ArgumentCaptor.forClass(String.class);
        InOrder order = inOrder(repository, sessionContextService);
        order.verify(repository).existsUser("target");
        order.verify(sessionContextService).blockSessionCreation(eq("target"), operationIdCaptor.capture());
        order.verify(sessionContextService).deleteByUserId("target");
        order.verify(repository).deleteUserRoles("target");
        order.verify(repository).deleteUser("target");
        assertThat(operationIdCaptor.getValue()).isNotBlank();
        onlySynchronization().afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        // 물리 삭제 성공 후 tombstone을 유지해 경합 중인 로그인 세션 발급을 계속 차단한다.
        verify(sessionContextService, never()).unblockSessionCreation(eq("target"), anyString());
    }

    @Test
    void secondDeleteUsesDistinctTokenAndDoesNotCompensateAnotherOwnerMarker() {
        when(repository.existsUser("target")).thenReturn(true);
        when(sessionContextService.blockSessionCreation(eq("target"), anyString())).thenReturn(true, false);
        when(repository.deleteUser("target")).thenReturn(1);

        service.deleteUser("target", "admin", "ROLE_ADMIN");
        Throwable secondDeleteFailure =
                catchThrowable(() -> service.deleteUser("target", "admin", "ROLE_ADMIN"));

        assertThat(secondDeleteFailure).isInstanceOf(BwgBusinessException.class);
        BwgBusinessException concurrentDelete = (BwgBusinessException) secondDeleteFailure;
        assertThat(concurrentDelete.getCode()).isEqualTo(BusinessErrorCode.BUSINESS_RULE_VIOLATION);
        assertThat(concurrentDelete.getDetails())
                .containsEntry("reason", "deleteAlreadyInProgress")
                .containsEntry("userId", "target");
        ArgumentCaptor<String> operationIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionContextService, times(2))
                .blockSessionCreation(eq("target"), operationIdCaptor.capture());
        assertThat(operationIdCaptor.getAllValues()).hasSize(2).doesNotHaveDuplicates();
        verify(sessionContextService, times(1)).deleteByUserId("target");
        verify(repository, times(1)).deleteUserRoles("target");
        verify(repository, times(1)).deleteUser("target");
        verify(sessionContextService, never()).unblockSessionCreation(eq("target"), anyString());
    }

    @Test
    void blockExceptionCompensatesWithTheAttemptedOperationToken() {
        when(repository.existsUser("target")).thenReturn(true);
        RedisConnectionFailureException redisFailure =
                new RedisConnectionFailureException("Redis is unavailable");
        doThrow(redisFailure)
                .when(sessionContextService)
                .blockSessionCreation(eq("target"), anyString());

        Throwable thrown = catchThrowable(() -> service.deleteUser("target", "admin", "ROLE_ADMIN"));

        assertThat(thrown).isInstanceOf(BwgAuthException.class).hasCause(redisFailure);
        assertThat(((BwgAuthException) thrown).getCode()).isEqualTo(CommonErrorCode.SERVICE_UNAVAILABLE);
        ArgumentCaptor<String> operationIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionContextService).blockSessionCreation(eq("target"), operationIdCaptor.capture());
        // SET NX 적용 뒤 응답만 유실됐을 수 있으므로 시도한 동일 토큰으로 compare-delete한다.
        verify(sessionContextService)
                .unblockSessionCreation("target", operationIdCaptor.getValue());
        verify(sessionContextService, never()).deleteByUserId("target");
        verify(repository, never()).deleteUserRoles("target");
        verify(repository, never()).deleteUser("target");
    }

    @Test
    void failedDeleteImmediatelyUnblocksWithTheCapturedOperationToken() {
        when(repository.existsUser("target")).thenReturn(true);
        when(sessionContextService.blockSessionCreation(eq("target"), anyString())).thenReturn(true);
        when(repository.deleteUser("target")).thenReturn(0);

        assertThatThrownBy(() -> service.deleteUser("target", "admin", "ROLE_ADMIN"))
                .isInstanceOf(BwgAuthException.class)
                .extracting("code")
                .isEqualTo(CommonErrorCode.DB_SAVE_DATA_ERROR);

        ArgumentCaptor<String> operationIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionContextService).blockSessionCreation(eq("target"), operationIdCaptor.capture());
        verify(sessionContextService)
                .unblockSessionCreation("target", operationIdCaptor.getValue());
    }

    @Test
    void transactionRollbackCompensatesTheOwnedTombstone() {
        when(repository.existsUser("target")).thenReturn(true);
        when(sessionContextService.blockSessionCreation(eq("target"), anyString())).thenReturn(true);
        when(repository.deleteUser("target")).thenReturn(1);

        service.deleteUser("target", "admin", "ROLE_ADMIN");

        ArgumentCaptor<String> operationIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionContextService).blockSessionCreation(eq("target"), operationIdCaptor.capture());
        onlySynchronization().afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        verify(sessionContextService)
                .unblockSessionCreation("target", operationIdCaptor.getValue());
    }

    private TransactionSynchronization onlySynchronization() {
        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        assertThat(synchronizations).hasSize(1);
        return synchronizations.get(0);
    }

    private ApiRequest<UserCreateReqDto> createRequest(String userId) {
        UserCreateReqDto data = new UserCreateReqDto();
        data.setUsrId(userId);
        data.setUsrNm("신규 사용자");
        data.setUsrPwd("password");
        ApiRequest<UserCreateReqDto> request = new ApiRequest<>();
        request.setData(data);
        return request;
    }
}
