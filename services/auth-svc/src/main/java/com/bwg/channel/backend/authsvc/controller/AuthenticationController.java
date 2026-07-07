package com.bwg.channel.backend.authsvc.controller;

import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.service.AuthenticationService;
import com.bwg.channel.backend.authsvc.token.cookie.RefreshTokenCookieSupport;
import com.bwg.channel.backend.common.config.OpenApiSupport;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.securitycommon.constants.AuthErrorCode;
import com.bwg.channel.backend.securitycommon.constants.InternalAuthHeaders;
import com.bwg.channel.backend.securitycommon.exception.BwgAuthException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인, 토큰 재발급, 로그아웃 요청에서 refresh token 쿠키와 인증 서비스를 연결하는 컨트롤러.
 */
@Tag(name = "인증")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    /** 로그인/토큰 재발급/로그아웃의 실제 인증 처리 서비스. */
    private final AuthenticationService authenticationService;

    /** refresh token을 응답 본문 대신 HttpOnly 쿠키로 전달하기 위한 쿠키 지원 객체. */
    private final RefreshTokenCookieSupport refreshTokenCookieSupport;

    /**
     * ERP 연동 로그인 요청을 API 저장소 인증 흐름으로 전달하고 refresh token을 쿠키로 내려준다.
     */
    @Operation(summary = "ERP 로그인", description = "ERP 연동 계정으로 로그인하고 토큰을 발급한다.")
    @PostMapping("erp-login")
    public ApiResponse<LoginResDto> erpLogin(@RequestBody ApiRequest<LoginReqDto> req, HttpServletResponse response) {
        return writeRefreshCookie(doLogin(req, "apiLogin"), response);
    }

    /**
     * 일반 로그인 요청을 MyBatis 저장소 인증 흐름으로 전달하고 refresh token을 응답 본문에서 숨긴다.
     */
    @Operation(summary = "일반 로그인", description = "사용자 ID와 비밀번호로 로그인하고 토큰을 발급한다.")
    @PostMapping("login")
    public ApiResponse<LoginResDto> login(@RequestBody ApiRequest<LoginReqDto> req, HttpServletResponse response) {
        return writeRefreshCookie(doLogin(req, "mybatisLogin"), response);
    }

    /**
     * 로그인 필수값을 검증한 뒤 지정된 저장소 type으로 인증 서비스를 호출한다.
     */
    private ApiResponse<LoginResDto> doLogin(ApiRequest<LoginReqDto> req, String type) {
        // 필수값 누락 시 인증 서비스 조회 전에 공통 실패 응답 반환
        final String requiredChkCode = AuthErrorCode.REQUIRED_VALUE_MISSING.getCode();
        final String requiredChkMsg = AuthErrorCode.REQUIRED_VALUE_MISSING.getMsg();
        LoginReqDto data = req == null ? null : req.getData();
        if (data == null || StringUtils.isBlank(data.getUsrId())) {
            return ApiResponse.fail(requiredChkCode, requiredChkMsg + " (아이디)");
        }
        if (StringUtils.isBlank(data.getUsrPwd())) {
            return ApiResponse.fail(requiredChkCode, requiredChkMsg + " (비밀번호)");
        }

        // 검증된 로그인 요청을 저장소 type과 함께 서비스 계층으로 전달
        return authenticationService.login(req, type);
    }

    /**
     * 브라우저가 자동 전송한 refresh token 쿠키를 검증해 새 access/refresh token을 발급받는다.
     */
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 Access Token을 재발급한다.")
    @PostMapping("/refresh-token")
    public ApiResponse<LoginResDto> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // HttpOnly 쿠키에서 refresh token 추출
        String refreshToken = refreshTokenCookieSupport.resolveRefreshToken(request);
        if (StringUtils.isBlank(refreshToken)) {
            throw new BwgAuthException.Builder()
                    .code(AuthErrorCode.INVALID_TOKEN)
                    .message("Refresh token cookie is missing")
                    .build();
        }

        // 쿠키 refresh token을 서비스 요청 DTO로 변환
        RefreshTknReqDto paramDto = new RefreshTknReqDto();
        paramDto.setRefreshToken(refreshToken);

        // 검증된 refresh token 기준 재발급 결과를 쿠키/본문 응답으로 분리
        return writeRefreshCookie(authenticationService.refreshToken(paramDto, "mybatisLogin"), response);
    }

    /**
     * Gateway 내부 인증 헤더의 사용자/sessionId로 DB refresh token과 Redis 세션을 제거한다.
     */
    @Operation(summary = "로그아웃", description = "Refresh Token과 Redis 세션 컨텍스트를 제거한다.")
    @SecurityRequirement(name = OpenApiSupport.BEARER_SCHEME)
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(InternalAuthHeaders.USER) String userId,
            @RequestHeader(InternalAuthHeaders.SESSION_ID) String sessionId,
            HttpServletResponse response
    ) {
        // 내부 인증 헤더의 사용자/sessionId 기준 로그아웃 처리
        ApiResponse<Void> apiResponse = authenticationService.logout(userId, sessionId, "mybatisLogin");

        // 브라우저에 남아 있는 refresh token 쿠키 만료
        refreshTokenCookieSupport.clearRefreshTokenCookie(response);
        return apiResponse;
    }

    /**
     * refresh token은 HttpOnly 쿠키로만 전달하고 응답 본문에는 access token 중심 payload를 유지한다.
     */
    private ApiResponse<LoginResDto> writeRefreshCookie(ApiResponse<LoginResDto> apiResponse, HttpServletResponse response) {
        // 서비스 응답 payload에 refresh token이 있을 때만 Set-Cookie 헤더 생성
        LoginResDto payload = apiResponse.getPayload();
        if (payload != null && StringUtils.isNotBlank(payload.getRefreshToken())) {
            refreshTokenCookieSupport.addRefreshTokenCookie(response, payload.getRefreshToken());
        }
        return apiResponse;
    }
}
