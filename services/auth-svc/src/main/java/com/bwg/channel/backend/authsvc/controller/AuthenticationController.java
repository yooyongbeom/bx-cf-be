package com.bwg.channel.backend.authsvc.controller;

import com.bwg.channel.backend.authsvc.config.RefreshTokenCookieSupport;
import com.bwg.channel.backend.authsvc.domain.dto.LoginDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.service.AuthenticationService;
import com.bwg.channel.backend.authcore.constants.AuthErrorCode;
import com.bwg.channel.backend.authcore.exception.BwgAuthException;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인과 토큰 재발급 API를 제공하는 인증 컨트롤러.
 */
@Tag(name = "인증")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenCookieSupport refreshTokenCookieSupport;

//    @GetMapping("err-test")
//    public ApiResponse<LoginDto> errTest() {
//        LoginDto paramDto = new LoginDto();
//        paramDto.setUsrId("1.yoo");
//        paramDto.setUsrPwd("1");
//        return authenticationService.erpLogin(paramDto);
//    }

    /**
     * ERP 인증 저장소를 사용해 로그인하고 refresh token은 HttpOnly 쿠키로 내려준다.
     */
    @Operation(summary = "ERP 로그인", description = "ERP 연동 계정으로 로그인하여 토큰을 발급한다.")
    @PostMapping("erp-login")
    public ApiResponse<LoginDto> erpLogin(@RequestBody LoginDto paramDto, HttpServletResponse response) {
        ApiResponse<LoginDto> apiResponse = doLogin(paramDto, "apiLogin");
        writeRefreshCookieAndHideToken(apiResponse, response);
        return apiResponse;
    }

    /**
     * 일반 사용자 계정으로 로그인하고 refresh token은 응답 본문에서 숨긴다.
     */
    @Operation(summary = "일반 로그인", description = "사용자 ID/비밀번호로 로그인하여 토큰을 발급한다.")
    @PostMapping("login")
    public ApiResponse<LoginDto> login(@RequestBody LoginDto paramDto, HttpServletResponse response) {
        ApiResponse<LoginDto> apiResponse = doLogin(paramDto, "mybatisLogin");
        writeRefreshCookieAndHideToken(apiResponse, response);
        return apiResponse;
    }

    /**
     * 로그인 필수값을 검증한 뒤 지정된 인증 저장소 전략으로 로그인한다.
     */
    private ApiResponse<LoginDto> doLogin(LoginDto paramDto, String type) {
        // 필수값 체크
        final String requiredChkCode = AuthErrorCode.REQUIRED_VALUE_MISSING.getCode();
        final String requiredChkMsg = AuthErrorCode.REQUIRED_VALUE_MISSING.getMsg();
        if (StringUtils.isBlank(paramDto.getUsrId())) {
            return ApiResponse.fail(requiredChkCode, requiredChkMsg + " (아이디)");
        }
        if (StringUtils.isBlank(paramDto.getUsrPwd())) {
            return ApiResponse.fail(requiredChkCode, requiredChkMsg + " (패스워드)");
        }

        return authenticationService.login(paramDto, type);
    }

    /**
     * 브라우저가 자동 전송한 refresh token 쿠키를 검증해 새 토큰을 발급한다.
     */
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 Access Token을 재발급한다.")
    @PostMapping("/refresh-token")
    public ApiResponse<LoginDto> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = refreshTokenCookieSupport.resolveRefreshToken(request);
        if (StringUtils.isBlank(refreshToken)) {
            throw new BwgAuthException.Builder()
                    .code(AuthErrorCode.INVALID_TOKEN)
                    .message("Refresh token cookie is missing")
                    .build();
        }

        RefreshTknReqDto paramDto = new RefreshTknReqDto();
        paramDto.setRefreshToken(refreshToken);

        ApiResponse<LoginDto> apiResponse = authenticationService.refreshToken(paramDto, "mybatisLogin");
        writeRefreshCookieAndHideToken(apiResponse, response);
        return apiResponse;
    }

    /**
     * refresh token을 쿠키로 기록하고 응답 본문에서는 민감 토큰 값을 제거한다.
     */
    private void writeRefreshCookieAndHideToken(ApiResponse<LoginDto> apiResponse, HttpServletResponse response) {
        LoginDto payload = apiResponse.getPayload();
        if (payload == null || StringUtils.isBlank(payload.getRefreshToken())) {
            return;
        }

        refreshTokenCookieSupport.addRefreshTokenCookie(response, payload.getRefreshToken());
        payload.setRefreshToken(null);
        payload.setRefreshTokenExpiresAt(null);
    }
}
