package com.bwg.channel.backend.authsvc.controller;

import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.service.AuthenticationService;
import com.bwg.channel.backend.authsvc.token.cookie.RefreshTokenCookieProperties;
import com.bwg.channel.backend.authsvc.token.cookie.RefreshTokenCookieSupport;
import com.bwg.channel.backend.common.aop.BwgAuthExceptionAdvice;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.securitycommon.constants.InternalAuthHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationCookieControllerTest {

    private final AuthenticationService authenticationService = mock(AuthenticationService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AuthenticationController(
                    authenticationService,
                    new RefreshTokenCookieSupport(new RefreshTokenCookieProperties())
            ))
            .setControllerAdvice(new BwgAuthExceptionAdvice())
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void loginSetsRefreshTokenCookieAndRemovesRefreshTokenFromBody() throws Exception {
        LoginResDto loginResponse = loginResponse();
        when(authenticationService.login(org.mockito.ArgumentMatchers.any(), eq("mybatisLogin")))
                .thenReturn(com.bwg.channel.backend.common.domain.dto.ApiResponse.success(loginResponse));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiRequest(loginRequest()))))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("refreshToken=refresh.jwt.token"),
                        org.hamcrest.Matchers.containsString("HttpOnly"),
                        org.hamcrest.Matchers.containsString("Path=/channel/backend/api/v1/auth"),
                        org.hamcrest.Matchers.containsString("SameSite=Lax")
                ))))
                .andExpect(jsonPath("$.payload.accessToken").value("access.jwt.token"))
                .andExpect(jsonPath("$.payload.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.payload.refreshTokenExpiresAt").doesNotExist());
    }

    @Test
    void refreshTokenReadsRefreshTokenFromCookieOnlyAndRotatesCookie() throws Exception {
        LoginResDto refreshResponse = loginResponse();
        refreshResponse.setAccessToken("new.access.jwt.token");
        refreshResponse.setRefreshToken("new.refresh.jwt.token");
        when(authenticationService.refreshToken(org.mockito.ArgumentMatchers.any(RefreshTknReqDto.class), eq("mybatisLogin")))
                .thenReturn(com.bwg.channel.backend.common.domain.dto.ApiResponse.success(refreshResponse));

        mockMvc.perform(post("/refresh-token")
                        .cookie(new Cookie("refreshToken", "old.refresh.jwt.token")))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist("requestBodyRefreshToken"))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("refreshToken=new.refresh.jwt.token"),
                        org.hamcrest.Matchers.containsString("HttpOnly"),
                        org.hamcrest.Matchers.containsString("Path=/channel/backend/api/v1/auth"),
                        org.hamcrest.Matchers.containsString("SameSite=Lax")
                ))))
                .andExpect(jsonPath("$.payload.accessToken").value("new.access.jwt.token"))
                .andExpect(jsonPath("$.payload.refreshToken").doesNotExist());

        ArgumentCaptor<RefreshTknReqDto> captor = ArgumentCaptor.forClass(RefreshTknReqDto.class);
        verify(authenticationService).refreshToken(captor.capture(), eq("mybatisLogin"));
        assertThat(captor.getValue().getRefreshToken()).isEqualTo("old.refresh.jwt.token");
    }

    @Test
    void refreshTokenRejectsBodyRefreshTokenWithoutCookie() throws Exception {
        RefreshTknReqDto body = new RefreshTknReqDto();
        body.setRefreshToken("body.refresh.jwt.token");

        mockMvc.perform(post("/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("-1002"));

        verify(authenticationService, never()).refreshToken(org.mockito.ArgumentMatchers.any(), eq("mybatisLogin"));
    }

    @Test
    void logoutUsesInternalAuthHeadersAndClearsRefreshTokenCookie() throws Exception {
        when(authenticationService.logout("hong.gildong", "session-123", "mybatisLogin"))
                .thenReturn(ApiResponse.success(null));

        mockMvc.perform(post("/logout")
                        .header(InternalAuthHeaders.USER, "hong.gildong")
                        .header(InternalAuthHeaders.SESSION_ID, "session-123")
                        .cookie(new Cookie("refreshToken", "refresh.jwt.token")))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("refreshToken="),
                        org.hamcrest.Matchers.containsString("Max-Age=0"),
                        org.hamcrest.Matchers.containsString("HttpOnly"),
                        org.hamcrest.Matchers.containsString("Path=/channel/backend/api/v1/auth"),
                        org.hamcrest.Matchers.containsString("SameSite=Lax")
                ))))
                .andExpect(jsonPath("$.success").value(true));

        verify(authenticationService).logout("hong.gildong", "session-123", "mybatisLogin");
    }

    private LoginReqDto loginRequest() {
        LoginReqDto request = new LoginReqDto();
        request.setUsrId("hong.gildong");
        request.setUsrPwd("password");
        return request;
    }

    private ApiRequest<LoginReqDto> apiRequest(LoginReqDto data) {
        ApiRequest<LoginReqDto> request = new ApiRequest<>();
        request.setData(data);
        return request;
    }

    private LoginResDto loginResponse() {
        LoginResDto response = new LoginResDto();
        response.setUsrId("hong.gildong");
        response.setAccessToken("access.jwt.token");
        response.setAccessTokenExpiresAt("20260625150000");
        response.setRefreshToken("refresh.jwt.token");
        response.setRefreshTokenExpiresAt("20260702150000");
        response.setRoles(List.of("USER"));
        return response;
    }
}
