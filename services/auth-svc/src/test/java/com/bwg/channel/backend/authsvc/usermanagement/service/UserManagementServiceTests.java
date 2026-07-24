package com.bwg.channel.backend.authsvc.usermanagement.service;

import com.bwg.channel.backend.authsvc.usermanagement.dto.UserCreateReqDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserDetailResDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserListResDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserUpdateReqDto;
import com.bwg.channel.backend.authsvc.usermanagement.repository.UserManagementRepository;
import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import com.bwg.channel.backend.common.constants.error.CommonErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.securitycommon.constants.AuthErrorCode;
import com.bwg.channel.backend.securitycommon.exception.BwgAuthException;
import com.bwg.channel.backend.sessioncontext.service.SessionContextService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserManagementServiceTests {

    private final UserManagementRepository repository = mock(UserManagementRepository.class);
    private final SessionContextService sessionContextService = mock(SessionContextService.class);
    private final UserManagementService service =
            new UserManagementServiceImpl(repository, sessionContextService);

    @Test
    void rejectsNonAdminBeforeRepositoryAccess() {
        assertThatThrownBy(() -> service.getUsers("ROLE_USER"))
                .isInstanceOf(BwgAuthException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.ACCESS_DENIED);

        verify(repository, never()).findUsers();
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
        data.setUsrPwd("password");
        ApiRequest<UserCreateReqDto> request = new ApiRequest<>();
        request.setData(data);

        when(repository.findRoleIdsByName("ROLE_USER")).thenReturn(List.of(7L));
        when(repository.insertUser(request, "admin")).thenReturn(1);
        when(repository.insertUserRole("new.user", 7L, "admin")).thenReturn(1);

        service.createUser(request, " admin ", "ROLE_ADMIN");

        assertThat(data.getUsrId()).isEqualTo("new.user");
        assertThat(data.getUsrNm()).isEqualTo("신규 사용자");
        InOrder order = inOrder(repository);
        order.verify(repository).existsUser("new.user");
        order.verify(repository).findRoleIdsByName("ROLE_USER");
        order.verify(repository).insertUser(request, "admin");
        order.verify(repository).insertUserRole("new.user", 7L, "admin");
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
                .isInstanceOf(BwgAuthException.class);

        when(repository.findRoleIdsByName("ROLE_USER")).thenReturn(List.of(1L, 2L));
        assertThatThrownBy(() -> service.createUser(request, "admin", "ROLE_ADMIN"))
                .isInstanceOf(BwgAuthException.class);
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
    void revokesAllTargetSessionsBeforeDeletingRolesAndUser() {
        when(repository.existsUser("target")).thenReturn(true);
        when(repository.deleteUser("target")).thenReturn(1);

        service.deleteUser("target", "admin", "ROLE_ADMIN");

        InOrder order = inOrder(repository, sessionContextService);
        order.verify(repository).existsUser("target");
        order.verify(sessionContextService).deleteByUserId("target");
        order.verify(repository).deleteUserRoles("target");
        order.verify(repository).deleteUser("target");
    }

    @Test
    void doesNotDeleteDatabaseRecordsWhenSessionRevocationFails() {
        when(repository.existsUser("target")).thenReturn(true);
        doThrow(new RuntimeException("Redis is unavailable"))
                .when(sessionContextService)
                .deleteByUserId("target");

        assertThatThrownBy(() -> service.deleteUser("target", "admin", "ROLE_ADMIN"))
                .isInstanceOf(BwgAuthException.class)
                .extracting("code")
                .isEqualTo(CommonErrorCode.SERVICE_UNAVAILABLE);

        verify(repository, never()).deleteUserRoles("target");
        verify(repository, never()).deleteUser("target");
    }

    @Test
    void keepsSessionsRevokedWhenUserDeletionFails() {
        when(repository.existsUser("target")).thenReturn(true);
        when(repository.deleteUser("target")).thenReturn(0);

        assertThatThrownBy(() -> service.deleteUser("target", "admin", "ROLE_ADMIN"))
                .isInstanceOf(BwgBusinessException.class);

        InOrder order = inOrder(repository, sessionContextService);
        order.verify(repository).existsUser("target");
        order.verify(sessionContextService).deleteByUserId("target");
        order.verify(repository).deleteUserRoles("target");
        order.verify(repository).deleteUser("target");
    }
}
