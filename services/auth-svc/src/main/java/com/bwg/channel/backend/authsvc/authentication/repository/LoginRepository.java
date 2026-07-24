package com.bwg.channel.backend.authsvc.authentication.repository;

import com.bwg.channel.backend.authsvc.authentication.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.authentication.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.authentication.dto.RefreshTknReqDto;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;

/**
 * 로그인 사용자 조회와 refresh token 저장을 추상화한 저장소 계약.
 */
public interface LoginRepository {
    /**
     * 사용자 ID와 비밀번호로 사용자 정보를 조회한다.
     *
     * @param loginReqDto 로그인 요청 파라미터
     * @return 토큰 정보를 제외한 사용자 정보
     */
    LoginResDto findByUsrIdAndUsrPwd(ApiRequest<LoginReqDto> loginReqDto);

    /**
     * 사용자별 refresh token 저장값을 갱신하거나 제거한다.
     *
     * @param loginResDto 사용자 ID와 저장할 refresh token 정보
     * @return 갱신된 행 수
     */
    int updateRefreshToken(LoginResDto loginResDto);

    /**
     * refresh token으로 사용자 정보를 조회한다.
     *
     * @param refreshTknReqDto refresh token 요청 정보
     * @return 토큰 소유자 정보
     */
    LoginResDto findByRefreshToken(RefreshTknReqDto refreshTknReqDto);
}
