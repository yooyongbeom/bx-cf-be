package com.bwg.channel.backend.authsvc.repository;

import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;

/**
 * 로그인 사용자 조회와 refresh token 저장을 추상화한 저장소 계약.
 */
public interface LoginRepository {
    /**
     * 사용자 ID와 비밀번호로 사용자 정보를 조회 (토큰 정보 제외)
     * @param loginReqDto 로그인 파라미터 (usrId, usrPwd)
     * @return 사용자 정보 (토큰은 서비스에서 채움)
    */
    LoginResDto findByUsrIdAndUsrPwd(LoginReqDto loginReqDto);

    /**
     * Refresh Token 정보를 업데이트
     * @param loginResDto 업데이트할 사용자 정보 (usrId, refreshToken, refreshTokenExpiresAt)
     * @return 업데이트된 행의 수
    */
    int updateRefreshToken(LoginResDto loginResDto);

    /**
     * Refresh token으로 사용자 정보를 조회
     * @param refreshTknReqDto Refresh Token 정보
     * @return 사용자 정보
     */
    LoginResDto findByRefreshToken(RefreshTknReqDto refreshTknReqDto);
}
