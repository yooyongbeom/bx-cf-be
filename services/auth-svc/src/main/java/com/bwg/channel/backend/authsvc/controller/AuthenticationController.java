package com.bwg.channel.backend.authsvc.controller;

import com.bwg.channel.backend.authsvc.domain.dto.LoginDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.service.AuthenticationService;
import com.bwg.channel.backend.authcore.constants.AuthErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("erp-login")
    public ApiResponse<LoginDto> erpLogin(@RequestBody LoginDto paramDto) {
        return doLogin(paramDto, "apiLogin");
    }

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

    @PostMapping("/refresh-token")
    public ApiResponse<LoginDto> refreshToken(@RequestBody RefreshTknReqDto paramDto) {
        return authenticationService.refreshToken(paramDto, "mybatisLogin");
    }
}
