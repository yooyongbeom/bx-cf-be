package com.bwg.channel.backend.authsvc.service;

import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;

/**
 * 로그인, refresh token 재발급, 로그아웃 흐름을 서비스 계층에서 제공하는 인증 계약.
 */
public interface AuthenticationService {

    /**
     * 지정된 로그인 저장소 type으로 사용자 정보를 조회하고 access/refresh token을 발급한다.
     */
    ApiResponse<LoginResDto> login(LoginReqDto paramDto, String type);

    /**
     * 검증된 refresh token으로 사용자 정보를 조회하고 새 access/refresh token으로 교체한다.
     */
    ApiResponse<LoginResDto> refreshToken(RefreshTknReqDto paramDto, String type);

    /**
     * 내부 인증 헤더의 사용자/sessionId 기준으로 DB refresh token과 Redis 세션을 제거한다.
     */
    ApiResponse<Void> logout(String userId, String sessionId, String type);
}
