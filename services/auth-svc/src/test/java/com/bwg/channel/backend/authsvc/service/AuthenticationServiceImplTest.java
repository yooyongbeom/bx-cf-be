package com.bwg.channel.backend.authsvc.service;

import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.repository.LoginRepository;
import com.bwg.channel.backend.authsvc.repository.jpa.JpaLoginRepository;
import com.bwg.channel.backend.securitycommon.util.JwtUtil;
import com.bwg.channel.backend.sessioncontext.domain.SessionContext;
import com.bwg.channel.backend.sessioncontext.service.SessionContextService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

        when(loginRepository.findByUsrIdAndUsrPwd(any(LoginReqDto.class))).thenReturn(loginUser);
        when(jwtUtil.createAccessToken(eq("hong.gildong"), eq(List.of("USER", "MANAGER")), anyString()))
                .thenReturn("access.jwt.token");
        when(jwtUtil.createRefreshToken("hong.gildong")).thenReturn("refresh.jwt.token");
        when(jwtUtil.getExpirationDateFromToken("access.jwt.token"))
                .thenReturn(new Date(System.currentTimeMillis() + Duration.ofMinutes(10).toMillis()));
        when(jwtUtil.getExpirationDateFromToken("refresh.jwt.token"))
                .thenReturn(new Date(System.currentTimeMillis() + Duration.ofDays(7).toMillis()));

        authenticationService.login(new LoginReqDto(), "mybatisLogin");

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
}
