package com.bwg.channel.backend.authsvc.service;

import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.repository.LoginRepository;
import com.bwg.channel.backend.authsvc.repository.jpa.JpaLoginRepository;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.securitycommon.constants.AuthErrorCode;
import com.bwg.channel.backend.securitycommon.exception.BwgAuthException;
import com.bwg.channel.backend.securitycommon.util.JwtUtil;
import com.bwg.channel.backend.sessioncontext.domain.SessionContext;
import com.bwg.channel.backend.sessioncontext.exception.SessionCreationBlockedException;
import com.bwg.channel.backend.sessioncontext.service.SessionContextService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceImplTest {

    @Test
    void loginStoresSessionContextUntilRefreshTokenExpires() {
        LoginRepository loginRepository = mock(LoginRepository.class);
        LoginRepository defaultLoginRepository = mock(LoginRepository.class);
        JpaLoginRepository jpaLoginRepository = mock(JpaLoginRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        SessionContextService sessionContextService = mock(SessionContextService.class);
        AuthenticationServiceImpl authenticationService = new AuthenticationServiceImpl(
                Map.of("mybatisLogin", loginRepository),
                defaultLoginRepository,
                jpaLoginRepository,
                jwtUtil,
                sessionContextService
        );

        LoginResDto loginUser = new LoginResDto();
        loginUser.setUsrId("hong.gildong");
        loginUser.setRoles(List.of("USER", "MANAGER"));

        when(loginRepository.findByUsrIdAndUsrPwd(any())).thenReturn(loginUser);
        when(jwtUtil.createAccessToken(eq("hong.gildong"), eq(List.of("USER", "MANAGER")), anyString()))
                .thenReturn("access.jwt.token");
        when(jwtUtil.createRefreshToken("hong.gildong")).thenReturn("refresh.jwt.token");
        when(jwtUtil.getExpirationDateFromToken("access.jwt.token"))
                .thenReturn(new Date(System.currentTimeMillis() + Duration.ofMinutes(10).toMillis()));
        when(jwtUtil.getExpirationDateFromToken("refresh.jwt.token"))
                .thenReturn(new Date(System.currentTimeMillis() + Duration.ofDays(7).toMillis()));

        authenticationService.login(apiRequest(new LoginReqDto()), "mybatisLogin");

        ArgumentCaptor<SessionContext> contextCaptor = ArgumentCaptor.forClass(SessionContext.class);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionContextService).save(contextCaptor.capture(), ttlCaptor.capture());
        verify(jwtUtil).createAccessToken(eq("hong.gildong"), eq(List.of("USER", "MANAGER")), sessionIdCaptor.capture());

        SessionContext savedContext = contextCaptor.getValue();
        assertThat(savedContext.getSessionId()).isNotBlank();
        assertThat(savedContext.getSessionId()).isEqualTo(sessionIdCaptor.getValue());
        assertThat(savedContext.getUserId()).isEqualTo("hong.gildong");
        assertThat(savedContext.getRoles()).containsExactly("USER", "MANAGER");
        assertThat(savedContext.getAuthLevel()).isEqualTo("LOGIN");
        assertThat(savedContext.getLoginTime()).isNotNull();
        assertThat(savedContext.getLastAccessTime()).isNotNull();
        assertThat(ttlCaptor.getValue()).isGreaterThan(Duration.ofDays(6));

        verify(loginRepository).updateRefreshToken(loginUser);
        assertThat(loginUser.getAccessToken()).isEqualTo("access.jwt.token");
        assertThat(loginUser.getRefreshToken()).isEqualTo("refresh.jwt.token");
    }

    @Test
    void refreshTokenStoresNewSessionContextAndCreatesAccessTokenWithSessionId() {
        LoginRepository loginRepository = mock(LoginRepository.class);
        LoginRepository defaultLoginRepository = mock(LoginRepository.class);
        JpaLoginRepository jpaLoginRepository = mock(JpaLoginRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        SessionContextService sessionContextService = mock(SessionContextService.class);
        AuthenticationServiceImpl authenticationService = new AuthenticationServiceImpl(
                Map.of("mybatisLogin", loginRepository),
                defaultLoginRepository,
                jpaLoginRepository,
                jwtUtil,
                sessionContextService
        );

        RefreshTknReqDto refreshRequest = new RefreshTknReqDto();
        refreshRequest.setRefreshToken("old.refresh.jwt.token");

        LoginResDto loginUser = new LoginResDto();
        loginUser.setUsrId("hong.gildong");
        loginUser.setRoles(List.of("USER", "MANAGER"));

        when(jwtUtil.validateRefreshToken("old.refresh.jwt.token")).thenReturn(true);
        when(loginRepository.findByRefreshToken(refreshRequest)).thenReturn(loginUser);
        when(jwtUtil.createAccessToken(eq("hong.gildong"), eq(List.of("USER", "MANAGER")), anyString()))
                .thenReturn("new.access.jwt.token");
        when(jwtUtil.createRefreshToken("hong.gildong")).thenReturn("new.refresh.jwt.token");
        when(jwtUtil.getExpirationDateFromToken("new.access.jwt.token"))
                .thenReturn(new Date(System.currentTimeMillis() + Duration.ofMinutes(10).toMillis()));
        when(jwtUtil.getExpirationDateFromToken("new.refresh.jwt.token"))
                .thenReturn(new Date(System.currentTimeMillis() + Duration.ofDays(7).toMillis()));

        authenticationService.refreshToken(refreshRequest, "mybatisLogin");

        ArgumentCaptor<SessionContext> contextCaptor = ArgumentCaptor.forClass(SessionContext.class);
        ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionContextService).save(contextCaptor.capture(), any(Duration.class));
        verify(jwtUtil).createAccessToken(eq("hong.gildong"), eq(List.of("USER", "MANAGER")), sessionIdCaptor.capture());

        assertThat(contextCaptor.getValue().getSessionId()).isEqualTo(sessionIdCaptor.getValue());
        assertThat(contextCaptor.getValue().getUserId()).isEqualTo("hong.gildong");
        assertThat(loginUser.getAccessToken()).isEqualTo("new.access.jwt.token");
        assertThat(loginUser.getRefreshToken()).isEqualTo("new.refresh.jwt.token");
    }

    @Test
    void loginTranslatesBlockedSessionCreationToSafeDenialAndPreservesCause() {
        LoginRepository loginRepository = mock(LoginRepository.class);
        LoginRepository defaultLoginRepository = mock(LoginRepository.class);
        JpaLoginRepository jpaLoginRepository = mock(JpaLoginRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        SessionContextService sessionContextService = mock(SessionContextService.class);
        AuthenticationServiceImpl authenticationService = new AuthenticationServiceImpl(
                Map.of("mybatisLogin", loginRepository),
                defaultLoginRepository,
                jpaLoginRepository,
                jwtUtil,
                sessionContextService
        );
        LoginResDto loginUser = new LoginResDto();
        loginUser.setUsrId("blocked-user");
        loginUser.setRoles(List.of("USER"));
        SessionCreationBlockedException blocked = mock(SessionCreationBlockedException.class);

        when(loginRepository.findByUsrIdAndUsrPwd(any())).thenReturn(loginUser);
        when(jwtUtil.createAccessToken(eq("blocked-user"), eq(List.of("USER")), anyString()))
                .thenReturn("access.jwt.token");
        when(jwtUtil.createRefreshToken("blocked-user")).thenReturn("refresh.jwt.token");
        when(jwtUtil.getExpirationDateFromToken("access.jwt.token"))
                .thenReturn(new Date(System.currentTimeMillis() + Duration.ofMinutes(10).toMillis()));
        when(jwtUtil.getExpirationDateFromToken("refresh.jwt.token"))
                .thenReturn(new Date(System.currentTimeMillis() + Duration.ofDays(7).toMillis()));
        doThrow(blocked).when(sessionContextService).save(any(SessionContext.class), any(Duration.class));

        Throwable thrown = catchThrowable(
                () -> authenticationService.login(apiRequest(new LoginReqDto()), "mybatisLogin")
        );

        assertThat(thrown).isInstanceOf(BwgAuthException.class).hasCause(blocked);
        assertThat(((BwgAuthException) thrown).getCode()).isEqualTo(AuthErrorCode.ACCESS_DENIED);
        // 세션 생성 거부가 DB refresh token 갱신 뒤 발생해도 원인을 보존한 예외로 트랜잭션을 롤백시킨다.
        verify(loginRepository).updateRefreshToken(loginUser);
    }

    @Test
    void refreshTranslatesBlockedSessionCreationToSafeDenialAndPreservesCause() {
        LoginRepository loginRepository = mock(LoginRepository.class);
        LoginRepository defaultLoginRepository = mock(LoginRepository.class);
        JpaLoginRepository jpaLoginRepository = mock(JpaLoginRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        SessionContextService sessionContextService = mock(SessionContextService.class);
        AuthenticationServiceImpl authenticationService = new AuthenticationServiceImpl(
                Map.of("mybatisLogin", loginRepository),
                defaultLoginRepository,
                jpaLoginRepository,
                jwtUtil,
                sessionContextService
        );
        RefreshTknReqDto refreshRequest = new RefreshTknReqDto();
        refreshRequest.setRefreshToken("old.refresh.jwt.token");
        LoginResDto loginUser = new LoginResDto();
        loginUser.setUsrId("blocked-user");
        loginUser.setRoles(List.of("USER"));
        SessionCreationBlockedException blocked = mock(SessionCreationBlockedException.class);

        when(jwtUtil.validateRefreshToken("old.refresh.jwt.token")).thenReturn(true);
        when(loginRepository.findByRefreshToken(refreshRequest)).thenReturn(loginUser);
        when(jwtUtil.createAccessToken(eq("blocked-user"), eq(List.of("USER")), anyString()))
                .thenReturn("new.access.jwt.token");
        when(jwtUtil.createRefreshToken("blocked-user")).thenReturn("new.refresh.jwt.token");
        when(jwtUtil.getExpirationDateFromToken("new.access.jwt.token"))
                .thenReturn(new Date(System.currentTimeMillis() + Duration.ofMinutes(10).toMillis()));
        when(jwtUtil.getExpirationDateFromToken("new.refresh.jwt.token"))
                .thenReturn(new Date(System.currentTimeMillis() + Duration.ofDays(7).toMillis()));
        doThrow(blocked).when(sessionContextService).save(any(SessionContext.class), any(Duration.class));

        Throwable thrown = catchThrowable(
                () -> authenticationService.refreshToken(refreshRequest, "mybatisLogin")
        );

        assertThat(thrown).isInstanceOf(BwgAuthException.class).hasCause(blocked);
        assertThat(((BwgAuthException) thrown).getCode()).isEqualTo(AuthErrorCode.ACCESS_DENIED);
        verify(loginRepository).updateRefreshToken(loginUser);
    }

    @Test
    void logoutClearsRefreshTokenAndDeletesSessionContext() {
        LoginRepository loginRepository = mock(LoginRepository.class);
        LoginRepository defaultLoginRepository = mock(LoginRepository.class);
        JpaLoginRepository jpaLoginRepository = mock(JpaLoginRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        SessionContextService sessionContextService = mock(SessionContextService.class);
        AuthenticationServiceImpl authenticationService = new AuthenticationServiceImpl(
                Map.of("mybatisLogin", loginRepository),
                defaultLoginRepository,
                jpaLoginRepository,
                jwtUtil,
                sessionContextService
        );

        authenticationService.logout("hong.gildong", "session-123", "mybatisLogin");

        ArgumentCaptor<LoginResDto> logoutUserCaptor = ArgumentCaptor.forClass(LoginResDto.class);
        verify(loginRepository).updateRefreshToken(logoutUserCaptor.capture());
        assertThat(logoutUserCaptor.getValue().getUsrId()).isEqualTo("hong.gildong");
        assertThat(logoutUserCaptor.getValue().getRefreshToken()).isNull();
        assertThat(logoutUserCaptor.getValue().getRefreshTokenExpiresAt()).isNull();
        verify(sessionContextService).deleteBySessionId("session-123");
    }

    private ApiRequest<LoginReqDto> apiRequest(LoginReqDto data) {
        ApiRequest<LoginReqDto> request = new ApiRequest<>();
        request.setData(data);
        return request;
    }
}
