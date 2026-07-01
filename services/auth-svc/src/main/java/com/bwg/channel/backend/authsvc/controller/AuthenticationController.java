package com.bwg.channel.backend.authsvc.controller;

import com.bwg.channel.backend.authsvc.config.RefreshTokenCookieSupport;
import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
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
 * <p>
 * 요청은 {@link LoginReqDto}, 응답은 {@link LoginResDto}를 그대로 사용한다. refresh token은
 * {@link LoginResDto}의 {@code @JsonIgnore} 필드로 서비스에서 전달되며, 컨트롤러가 이를 읽어
 * HttpOnly 쿠키로만 내려보내고 응답 본문에는 노출하지 않는다.
 */
@Tag(name = "인증")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenCookieSupport refreshTokenCookieSupport;

    /**
     * ERP 인증 저장소를 사용해 로그인하고 refresh token은 HttpOnly 쿠키로 내려준다.
     */
    @Operation(summary = "ERP 로그인", description = "ERP 연동 계정으로 로그인하여 토큰을 발급한다.")
    @PostMapping("erp-login")
    public ApiResponse<LoginResDto> erpLogin(@RequestBody LoginReqDto req, HttpServletResponse response) {
        return writeRefreshCookie(doLogin(req, "apiLogin"), response);
    }

    /**
     * 일반 사용자 계정으로 로그인하고 refresh token은 응답 본문에서 숨긴다.
     */
    @Operation(summary = "일반 로그인", description = "사용자 ID/비밀번호로 로그인하여 토큰을 발급한다.")
    @PostMapping("login")
    public ApiResponse<LoginResDto> login(@RequestBody LoginReqDto req, HttpServletResponse response) {
        return writeRefreshCookie(doLogin(req, "mybatisLogin"), response);
    }

    /**
     * 로그인 필수값을 검증한 뒤 지정된 인증 저장소 전략으로 로그인한다.
     */
    private ApiResponse<LoginResDto> doLogin(LoginReqDto req, String type) {
        // 필수값 체크
        final String requiredChkCode = AuthErrorCode.REQUIRED_VALUE_MISSING.getCode();
        final String requiredChkMsg = AuthErrorCode.REQUIRED_VALUE_MISSING.getMsg();
        if (StringUtils.isBlank(req.getUsrId())) {
            return ApiResponse.fail(requiredChkCode, requiredChkMsg + " (아이디)");
        }
        if (StringUtils.isBlank(req.getUsrPwd())) {
            return ApiResponse.fail(requiredChkCode, requiredChkMsg + " (패스워드)");
        }

        return authenticationService.login(req, type);
    }

    /**
     * 브라우저가 자동 전송한 refresh token 쿠키를 검증해 새 토큰을 발급한다.
     */
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 Access Token을 재발급한다.")
    @PostMapping("/refresh-token")
    public ApiResponse<LoginResDto> refreshToken(
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

        return writeRefreshCookie(authenticationService.refreshToken(paramDto, "mybatisLogin"), response);
    }

    /**
     * 응답에 담긴 refresh token(@JsonIgnore)을 HttpOnly 쿠키로 기록한다.
     * 응답 본문에는 {@code @JsonIgnore}로 인해 refresh token이 직렬화되지 않는다.
     */
    private ApiResponse<LoginResDto> writeRefreshCookie(ApiResponse<LoginResDto> apiResponse, HttpServletResponse response) {
        LoginResDto payload = apiResponse.getPayload();
        if (payload != null && StringUtils.isNotBlank(payload.getRefreshToken())) {
            refreshTokenCookieSupport.addRefreshTokenCookie(response, payload.getRefreshToken());
        }
        return apiResponse;
    }
}
