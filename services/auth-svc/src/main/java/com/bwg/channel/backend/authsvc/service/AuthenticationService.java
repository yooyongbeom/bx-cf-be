package com.bwg.channel.backend.authsvc.service;

import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;

/**
 * 로그인과 refresh token 재발급을 처리하는 인증 서비스 계약.
 */
public interface AuthenticationService {

    /**
     * 지정된 로그인 저장소 전략으로 사용자를 인증하고 access/refresh token을 발급한다.
     */
    ApiResponse<LoginResDto> login(LoginReqDto paramDto, String type);

    /**
     * 전달된 refresh token을 검증하고 새 access/refresh token으로 교체한다.
     */
    ApiResponse<LoginResDto> refreshToken(RefreshTknReqDto paramDto, String type);
}
