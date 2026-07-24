package com.bwg.channel.backend.authsvc.authentication.service;

import com.bwg.channel.backend.authsvc.authentication.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.authentication.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.authentication.dto.RefreshTknReqDto;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;

/**
 * 로그인, refresh token 재발급, 로그아웃 흐름을 서비스 계층에서 제공하는 인증 계약.
 */
public interface AuthenticationService {

    /**
     * 지정된 로그인 저장소 type으로 사용자 정보를 조회하고 access/refresh token을 발급한다.
     *
     * @param paramDto 사용자 ID와 비밀번호가 포함된 로그인 요청
     * @param type 사용할 로그인 저장소 유형
     * @return 사용자 정보와 access/refresh token이 포함된 로그인 응답
     */
    ApiResponse<LoginResDto> login(ApiRequest<LoginReqDto> paramDto, String type);

    /**
     * 검증된 refresh token으로 사용자 정보를 조회하고 새 access/refresh token으로 교체한다.
     *
     * @param paramDto 쿠키에서 추출한 refresh token 요청
     * @param type 사용할 로그인 저장소 유형
     * @return 새 access/refresh token이 포함된 재발급 응답
     */
    ApiResponse<LoginResDto> refreshToken(RefreshTknReqDto paramDto, String type);

    /**
     * 내부 인증 헤더의 사용자/sessionId 기준으로 DB refresh token과 Redis 세션을 제거한다.
     *
     * @param userId 로그아웃할 사용자 ID
     * @param sessionId 삭제할 Redis 세션 ID
     * @param type 사용할 로그인 저장소 유형
     * @return 로그아웃 성공 응답
     */
    ApiResponse<Void> logout(String userId, String sessionId, String type);
}
