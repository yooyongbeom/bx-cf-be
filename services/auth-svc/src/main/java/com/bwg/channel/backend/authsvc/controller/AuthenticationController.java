package com.bwg.channel.backend.authsvc.controller;

import com.bwg.channel.backend.authsvc.domain.dto.LoginDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.service.AuthenticationService;
import com.bwg.channel.backend.authcore.constants.AuthErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

//    @GetMapping("err-test")
//    public ApiResponse<LoginDto> errTest() {
//        LoginDto paramDto = new LoginDto();
//        paramDto.setUsrId("1.yoo");
//        paramDto.setUsrPwd("1");
//        return authenticationService.erpLogin(paramDto);
//    }

    @Operation(summary = "ERP 로그인", description = "ERP 연동 계정으로 로그인하여 토큰을 발급한다.")
    @PostMapping("erp-login")
    public ApiResponse<LoginDto> erpLogin(@RequestBody LoginDto paramDto) {
        return doLogin(paramDto, "apiLogin");
    }

    @Operation(summary = "일반 로그인", description = "사용자 ID/비밀번호로 로그인하여 토큰을 발급한다.")
    @PostMapping("login")
    public ApiResponse<LoginDto> login(@RequestBody LoginDto paramDto) {
        return doLogin(paramDto, "mybatisLogin");
    }

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

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 Access Token을 재발급한다.")
    @PostMapping("/refresh-token")
    public ApiResponse<LoginDto> refreshToken(@RequestBody RefreshTknReqDto paramDto) {
        return authenticationService.refreshToken(paramDto, "mybatisLogin");
    }
}
