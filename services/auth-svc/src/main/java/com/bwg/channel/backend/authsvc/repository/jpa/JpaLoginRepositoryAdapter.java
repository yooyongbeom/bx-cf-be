package com.bwg.channel.backend.authsvc.repository.jpa;

import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.domain.entity.Role;
import com.bwg.channel.backend.authsvc.domain.entity.User;
import com.bwg.channel.backend.authsvc.repository.LoginRepository;
import com.bwg.channel.backend.common.constants.enums.CommonErrorCode;
import com.bwg.channel.backend.securitycommon.exception.BwgAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component("jpaLogin")
@RequiredArgsConstructor
public class JpaLoginRepositoryAdapter implements LoginRepository {
    /** JPA 엔티티 기반 사용자와 refresh token 조회/갱신 저장소. */
    private final JpaLoginRepository jpaLoginRepository;

    @Override
    public LoginResDto findByUsrIdAndUsrPwd(LoginReqDto paramDto) {
        // 로그인 요청의 사용자 ID/PW 기준으로 JPA 사용자 엔티티 조회
        User user = jpaLoginRepository.findByUsrIdAndUsrPwd(paramDto.getUsrId(), paramDto.getUsrPwd())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(CommonErrorCode.DB_NO_DATA_ERROR)
                                .message(CommonErrorCode.DB_NO_DATA_ERROR.getMsg())
                                .details(null)
                                .build());

        return makeLoginResDto(user);
    }

    @Override
    public LoginResDto findByRefreshToken(RefreshTknReqDto refreshTknReqDto) {
        // 검증된 refresh token 값으로 JPA 사용자 엔티티 조회
        User user = jpaLoginRepository.findByRefreshToken(refreshTknReqDto.getRefreshToken())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(CommonErrorCode.DB_NO_DATA_ERROR)
                                .message(CommonErrorCode.DB_NO_DATA_ERROR.getMsg())
                                .details(null)
                                .build());

        return makeLoginResDto(user);
    }

    private LoginResDto makeLoginResDto(User user) {
        // 사용자 엔티티의 Role 연관관계에서 JWT claim에 넣을 권한명 목록 추출
        List<String> roles = user.getRoles().stream()
                .map(Role::getRoleNm)
                .collect(Collectors.toList());

        // 인증 서비스가 공통으로 사용할 로그인 응답 DTO로 변환
        LoginResDto loginResDto = new LoginResDto();
        loginResDto.setUsrId(user.getUsrId());
        loginResDto.setUsrNm(user.getUsrNm());
        loginResDto.setPositDivName(user.getPositDivName());
        loginResDto.setDeptName(user.getDeptName());
        loginResDto.setRoles(roles);

        return loginResDto;
    }

    @Override
    @Transactional
    public int updateRefreshToken(LoginResDto loginResDto) {
        // 사용자 ID 기준 refresh token 저장 대상 엔티티 조회
        User user = jpaLoginRepository.findByUsrId(loginResDto.getUsrId())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(CommonErrorCode.DB_SAVE_DATA_ERROR)
                                .message("User not found for refresh token update")
                                .details(null)
                                .build());

        // refresh token rotation 결과와 만료 일시를 사용자 엔티티에 반영
        user.setRefreshToken(loginResDto.getRefreshToken());
        user.setRefreshTokenExpiresAt(loginResDto.getRefreshTokenExpiresAt());
        jpaLoginRepository.save(user);
        return 1; // Assuming success
    }
}
