package com.bwg.channel.backend.securitycommon.service;

import com.bwg.channel.backend.securitycommon.domain.dto.CustomUserDetails;
import com.bwg.channel.backend.securitycommon.exception.UserNotFoundException;

/**
 * Spring Security가 사용자 이름으로 인증 주체를 조회할 때 사용하는 서비스 계약.
 */
public interface CustomUserDetailsService {

    /**
     * 사용자 이름에 해당하는 보안 사용자 정보를 조회한다.
     *
     * @param username 조회할 사용자 이름
     * @return 인증과 권한 확인에 사용할 사용자 상세정보
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    CustomUserDetails loadUserByUsername(String username) throws UserNotFoundException;
}
