package com.bwg.channel.backend.authsvc.service;

import com.bwg.channel.backend.authcore.domain.dto.CustomUserDetails;
import com.bwg.channel.backend.authcore.exception.UserNotFoundException;
import com.bwg.channel.backend.authcore.service.CustomUserDetailsService;
import com.bwg.channel.backend.authcore.util.JwtUtil;
import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.repository.LoginRepository;
import com.bwg.channel.backend.authsvc.repository.jpa.JpaLoginRepository;
import com.bwg.channel.backend.authcore.constants.AuthErrorCode;
import com.bwg.channel.backend.authcore.exception.BwgAuthException;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 로그인 저장소 전략을 선택해 인증을 수행하고 JWT 발급/갱신을 담당한다.
 */
@Slf4j
@Service("authenticationService")
public class AuthenticationServiceImpl implements AuthenticationService, CustomUserDetailsService {

    private final Map<String, LoginRepository> loginRepositoryMap;
    private final LoginRepository defaultLoginRepository;
    private final JwtUtil jwtUtil;

    public AuthenticationServiceImpl(Map<String, LoginRepository> loginRepositoryMap,
                                     LoginRepository defaultLoginRepository,
                                     JpaLoginRepository jpaLoginRepository,
                                     JwtUtil jwtUtil) {
        this.loginRepositoryMap = loginRepositoryMap;
        this.defaultLoginRepository = defaultLoginRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 사용자 인증 후 access token과 refresh token을 생성하고 refresh token을 저장한다.
     */
    @Override
    @Transactional
    public ApiResponse<LoginResDto> login(LoginReqDto paramDto, String type) {
        log.info("current login type =====================> {}", type);

        LoginRepository repo = getRepo(type);

        // 1. 사용자 정보 조회 (전략에 따라 API, JPA, MyBatis 호출)
        LoginResDto userDetails = repo.findByUsrIdAndUsrPwd(paramDto);

        // 2. Access Token 및 Refresh Token 생성
        final String accessToken = jwtUtil.createAccessToken(userDetails.getUsrId(), userDetails.getRoles());
        final String refreshToken = jwtUtil.createRefreshToken(userDetails.getUsrId());

        // 3. Refresh Token 정보를 DB에 업데이트
        userDetails.setRefreshToken(refreshToken);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        // Access Token 만료 시간 설정
        Date accessTokenExpiryDate = jwtUtil.getExpirationDateFromToken(accessToken);
        userDetails.setAccessTokenExpiresAt(sdf.format(accessTokenExpiryDate));
        // Refresh Token 만료 시간 설정
        Date refreshTokenExpiryDate = jwtUtil.getExpirationDateFromToken(refreshToken);
        userDetails.setRefreshTokenExpiresAt(sdf.format(refreshTokenExpiryDate));

        repo.updateRefreshToken(userDetails);

        // 4. DTO에 Access Token 추가하여 반환 (비밀번호는 LoginResDto에 아예 없음)
        userDetails.setAccessToken(accessToken);

        return ApiResponse.success(userDetails);
    }

    /**
     * 기존 refresh token을 검증한 뒤 DB에 저장된 토큰을 새 refresh token으로 교체한다.
     */
    @Override
    @Transactional
    public ApiResponse<LoginResDto> refreshToken(RefreshTknReqDto paramDto, String type) {
        // 1. Refresh Token 검증
        String refreshToken = paramDto.getRefreshToken();
        try {
            jwtUtil.validateRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new BwgAuthException.Builder()
                    .code(AuthErrorCode.INVALID_TOKEN)
                    .message("Invalid Refresh Token: " + e.getMessage())
                    .build();
        }

        LoginRepository repo = getRepo(type);

        // 1. Refresh Token으로 사용자 정보 조회
        LoginResDto userDetails = repo.findByRefreshToken(paramDto);

        // 3. 새로운 토큰 생성
        String newAccessToken = jwtUtil.createAccessToken(userDetails.getUsrId(), userDetails.getRoles());
        String newRefreshToken = jwtUtil.createRefreshToken(userDetails.getUsrId());

        // 4. 새로운 Refresh Token 정보 DB에 업데이트
        userDetails.setRefreshToken(newRefreshToken);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        // Access Token 만료 시간 설정
        Date accessTokenExpiryDate = jwtUtil.getExpirationDateFromToken(newAccessToken);
        userDetails.setAccessTokenExpiresAt(sdf.format(accessTokenExpiryDate));
        // Refresh Token 만료 시간 설정
        Date refreshTokenExpiryDate = jwtUtil.getExpirationDateFromToken(newRefreshToken);
        userDetails.setRefreshTokenExpiresAt(sdf.format(refreshTokenExpiryDate));

        repo.updateRefreshToken(userDetails);

        // 5. 새로운 토큰 정보 DTO에 담아 반환 (비밀번호는 LoginResDto에 아예 없음)
        userDetails.setAccessToken(newAccessToken);
        userDetails.setRefreshToken(newRefreshToken);

        return ApiResponse.success(userDetails);
    }

    /**
     * Spring Security UserDetailsService 연동 지점이며 현재 인증 흐름에서는 별도 조회를 수행하지 않는다.
     */
    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UserNotFoundException {
        // 이 메서드는 Spring Security의 UserDetailsService에서 사용됨
        // 현재 구조에서는 erpLogin을 통해 인증하므로, 필요시 별도 구현 필요
//        User user = jpaLoginRepository.findByUsrId(username)
//                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
//
//        return new CustomUserDetails(
//                user.getUsrId(),
//                user.getUsrPwd(),
//                user.getRoles().stream()
//                        .map(role -> role.getRoleName())
//                        .collect(Collectors.toList())
//        );

        return null;
    }

    /**
     * 요청된 로그인 타입에 해당하는 저장소 전략을 선택한다.
     */
    private LoginRepository getRepo(String type) {
        LoginRepository repo = (type == null || type.isEmpty())
                ? defaultLoginRepository
                : loginRepositoryMap.get(type);

        if (repo == null) {
            throw new BwgAuthException.Builder()
                    .code(AuthErrorCode.REQUIRED_VALUE_MISSING)
                    .message("Invalid Login type : " + type)
                    .build();
        }

        return repo;
    }
}
