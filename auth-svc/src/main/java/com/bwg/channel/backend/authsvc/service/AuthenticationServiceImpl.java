package com.bwg.channel.backend.authsvc.service;

import com.bwg.channel.backend.authcore.domain.dto.CustomUserDetails;
import com.bwg.channel.backend.authcore.exception.UserNotFoundException;
import com.bwg.channel.backend.authcore.service.CustomUserDetailsService;
import com.bwg.channel.backend.authcore.util.JwtUtil;
import com.bwg.channel.backend.authsvc.domain.dto.LoginDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.repository.LoginRepository;
import com.bwg.channel.backend.authsvc.repository.jpa.JpaLoginRepository;
import com.bwg.channel.backend.common.constants.enums.AuthErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.exception.BwgAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

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

    @Override
    @Transactional
    public ApiResponse<LoginDto> login(LoginDto paramDto, String type) {
        log.info("current login type =====================> {}", type);

        LoginRepository repo = getRepo(type);

        // 1. 사용자 정보 조회 (전략에 따라 API, JPA, MyBatis 호출)
        LoginDto userDetails = repo.findByUsrIdAndUsrPwd(paramDto);

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

        // 4. DTO에 Access Token 추가하여 반환
        userDetails.setAccessToken(accessToken);
        // 비밀번호는 응답에서 제외
        userDetails.setUsrPwd(null);

        return ApiResponse.success(userDetails);
    }

    @Override
    @Transactional
    public ApiResponse<LoginDto> refreshToken(RefreshTknReqDto paramDto, String type) {
        // 1. Refresh Token 검증
        String refreshToken = paramDto.getRefreshToken();
        try {
            jwtUtil.validateToken(refreshToken);
        } catch (Exception e) {
            throw new BwgAuthException.Builder()
                    .code(AuthErrorCode.INVALID_TOKEN)
                    .message("Invalid Refresh Token: " + e.getMessage())
                    .build();
        }

        LoginRepository repo = getRepo(type);

        // 1. Refresh Token으로 사용자 정보 조회
        LoginDto userDetails = repo.findByRefreshToken(paramDto);

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

        // 5. 새로운 토큰 정보 DTO에 담아 반환
        userDetails.setUsrPwd(null);
        userDetails.setAccessToken(newAccessToken);
        userDetails.setRefreshToken(newRefreshToken);

        return ApiResponse.success(userDetails);
    }

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