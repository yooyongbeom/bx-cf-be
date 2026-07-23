package com.bwg.channel.backend.authsvc.service;

import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.repository.LoginRepository;
import com.bwg.channel.backend.authsvc.repository.jpa.JpaLoginRepository;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.securitycommon.constants.AuthErrorCode;
import com.bwg.channel.backend.securitycommon.domain.dto.CustomUserDetails;
import com.bwg.channel.backend.securitycommon.exception.BwgAuthException;
import com.bwg.channel.backend.securitycommon.exception.UserNotFoundException;
import com.bwg.channel.backend.securitycommon.service.CustomUserDetailsService;
import com.bwg.channel.backend.securitycommon.util.JwtUtil;
import com.bwg.channel.backend.sessioncontext.domain.SessionContext;
import com.bwg.channel.backend.sessioncontext.service.SessionContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * 로그인 저장소 선택, JWT 발급, refresh token 저장, Redis 세션 컨텍스트 저장 흐름을 묶는 인증 서비스.
 */
@Slf4j
@Service("authenticationService")
public class AuthenticationServiceImpl implements AuthenticationService, CustomUserDetailsService {
    /** Redis 세션 컨텍스트에 기록되는 기본 인증 수준. */
    private static final String DEFAULT_AUTH_LEVEL = "LOGIN";

    /** 로그인 type 값으로 JPA/MyBatis/API 저장소 구현체를 선택하기 위한 저장소 맵. */
    private final Map<String, LoginRepository> loginRepositoryMap;

    /** type 값이 없을 때 사용하는 기본 로그인 저장소. */
    private final LoginRepository defaultLoginRepository;

    /** access token, refresh token 발급과 claim 검증을 담당하는 JWT 유틸리티. */
    private final JwtUtil jwtUtil;

    /** 로그인 세션 정보를 Redis에 저장/조회/삭제하는 공통 서비스. */
    private final SessionContextService sessionContextService;

    public AuthenticationServiceImpl(Map<String, LoginRepository> loginRepositoryMap,
                                     LoginRepository defaultLoginRepository,
                                     JpaLoginRepository jpaLoginRepository,
                                     JwtUtil jwtUtil,
                                     SessionContextService sessionContextService) {
        this.loginRepositoryMap = loginRepositoryMap;
        this.defaultLoginRepository = defaultLoginRepository;
        this.jwtUtil = jwtUtil;
        this.sessionContextService = sessionContextService;
    }

    /**
     * 로그인 요청의 type 저장소에서 사용자 정보를 확인하고 access/refresh token과 Redis 세션을 함께 만든다.
     *
     * <p>발급한 refresh token은 DB에 저장하고, access token의 sessionId와 연결되는
     * 세션 컨텍스트는 refresh token 만료 시각을 TTL로 사용해 Redis에 저장한다.</p>
     *
     * @param paramDto 사용자 ID와 비밀번호가 포함된 로그인 요청
     * @param type 사용할 로그인 저장소 유형
     * @return 사용자 정보와 access/refresh token이 포함된 로그인 응답
     * @throws BwgAuthException 저장소 유형이 유효하지 않거나 인증 처리에 실패한 경우
     */
    @Override
    @Transactional
    public ApiResponse<LoginResDto> login(ApiRequest<LoginReqDto> paramDto, String type) {
        log.info("current login type =====================> {}", type);

        // 로그인 type에 맞는 저장소 선택 후 ID/PW 기준 사용자 정보 조회
        LoginRepository repo = getRepo(type);
        LoginResDto userDetails = repo.findByUsrIdAndUsrPwd(paramDto);

        // access token claim과 Redis key를 연결할 신규 세션 식별자 발급
        String sessionId = UUID.randomUUID().toString();

        // 조회된 사용자 ID/권한/sessionId 기반 access token 발급
        String accessToken = jwtUtil.createAccessToken(userDetails.getUsrId(), userDetails.getRoles(), sessionId);

        // 이후 토큰 재발급 요청에서 DB 검증 기준으로 사용할 refresh token 발급
        String refreshToken = jwtUtil.createRefreshToken(userDetails.getUsrId());
        userDetails.setRefreshToken(refreshToken);

        // 응답 DTO에 노출할 토큰 만료 시각 포맷터
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        // access token 만료 시각을 응답 DTO에 기록
        Date accessTokenExpiryDate = jwtUtil.getExpirationDateFromToken(accessToken);
        userDetails.setAccessTokenExpiresAt(sdf.format(accessTokenExpiryDate));

        // refresh token 만료 시각을 응답 DTO와 Redis TTL 계산 기준으로 기록
        Date refreshTokenExpiryDate = jwtUtil.getExpirationDateFromToken(refreshToken);
        userDetails.setRefreshTokenExpiresAt(sdf.format(refreshTokenExpiryDate));

        // 발급된 refresh token을 DB에 저장해 이후 재발급 요청 검증 기준으로 사용
        repo.updateRefreshToken(userDetails);

        // access token sessionId와 같은 값으로 Redis 세션 컨텍스트 저장
        saveSessionContext(userDetails, sessionId, refreshTokenExpiryDate);

        // access token은 응답 본문으로 전달하고 refresh token은 컨트롤러에서 HttpOnly 쿠키로 전달
        userDetails.setAccessToken(accessToken);
        return ApiResponse.success(userDetails);
    }

    /**
     * 로그인 또는 토큰 재발급 결과를 Redis 세션 컨텍스트로 저장한다.
     *
     * <p>세션 생성 시각과 마지막 접근 시각은 동일한 현재 시각을 사용하며,
     * refresh token 만료 시각까지의 기간을 Redis TTL로 설정한다.</p>
     *
     * @param userDetails 세션에 기록할 사용자와 권한 정보
     * @param sessionId access token claim과 Redis key에 사용할 세션 ID
     * @param refreshTokenExpiryDate Redis TTL 기준이 되는 refresh token 만료 시각
     */
    private void saveSessionContext(LoginResDto userDetails, String sessionId, Date refreshTokenExpiryDate) {
        // 세션 생성/마지막 접근 시각의 동일 기준 시각
        Instant now = Instant.now();

        // 내부 서비스가 공통으로 읽을 사용자 세션 컨텍스트 구성
        SessionContext sessionContext = SessionContext.builder()
                .sessionId(sessionId)
                .userId(userDetails.getUsrId())
                .roles(userDetails.getRoles())
                .authLevel(DEFAULT_AUTH_LEVEL)
                .loginTime(now)
                .lastAccessTime(now)
                .build();

        // refresh token 만료 시각과 Redis 세션 만료 시각을 맞추기 위한 TTL
        Duration ttl = Duration.between(now, refreshTokenExpiryDate.toInstant());

        // session:{sessionId} 규칙으로 Redis에 세션 컨텍스트 저장
        sessionContextService.save(sessionContext, ttl);
    }

    /**
     * 검증된 refresh token으로 사용자 정보를 조회하고 access/refresh token과 Redis 세션을 새로 발급한다.
     *
     * <p>refresh token을 검증한 후 신규 token 쌍과 sessionId를 만들고, DB의 refresh token과
     * Redis 세션 컨텍스트를 신규 값으로 저장한다.</p>
     *
     * @param paramDto 쿠키에서 추출한 refresh token 요청
     * @param type 사용할 로그인 저장소 유형
     * @return 새 access/refresh token이 포함된 재발급 응답
     * @throws BwgAuthException refresh token이 유효하지 않거나 저장소 유형이 유효하지 않은 경우
     */
    @Override
    @Transactional
    public ApiResponse<LoginResDto> refreshToken(RefreshTknReqDto paramDto, String type) {
        // 쿠키로 전달된 refresh token 값
        String refreshToken = paramDto.getRefreshToken();
        try {
            // refresh token 서명/만료/type claim 검증
            jwtUtil.validateRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new BwgAuthException.Builder()
                    .code(AuthErrorCode.INVALID_TOKEN)
                    .message("Invalid Refresh Token: " + e.getMessage())
                    .build();
        }

        // 검증된 refresh token으로 DB에 저장된 사용자 정보 조회
        LoginRepository repo = getRepo(type);
        LoginResDto userDetails = repo.findByRefreshToken(paramDto);

        // 토큰 재발급 시 access token claim과 Redis key를 새로 연결할 sessionId
        String newSessionId = UUID.randomUUID().toString();

        // 조회된 사용자 ID/권한/newSessionId 기반 신규 access token 발급
        String newAccessToken = jwtUtil.createAccessToken(userDetails.getUsrId(), userDetails.getRoles(), newSessionId);

        // refresh token rotation을 위한 신규 refresh token 발급
        String newRefreshToken = jwtUtil.createRefreshToken(userDetails.getUsrId());
        userDetails.setRefreshToken(newRefreshToken);

        // 재발급 응답 DTO에 노출할 토큰 만료 시각 포맷터
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        // 신규 access token 만료 시각을 응답 DTO에 기록
        Date accessTokenExpiryDate = jwtUtil.getExpirationDateFromToken(newAccessToken);
        userDetails.setAccessTokenExpiresAt(sdf.format(accessTokenExpiryDate));

        // 신규 refresh token 만료 시각을 응답 DTO와 Redis TTL 계산 기준으로 기록
        Date refreshTokenExpiryDate = jwtUtil.getExpirationDateFromToken(newRefreshToken);
        userDetails.setRefreshTokenExpiresAt(sdf.format(refreshTokenExpiryDate));

        // DB refresh token을 교체해 이전 refresh token 재사용을 차단
        repo.updateRefreshToken(userDetails);

        // 신규 access token sessionId와 같은 값으로 Redis 세션 컨텍스트 저장
        saveSessionContext(userDetails, newSessionId, refreshTokenExpiryDate);

        // 신규 access token은 응답 본문으로, 신규 refresh token은 컨트롤러에서 HttpOnly 쿠키로 전달
        userDetails.setAccessToken(newAccessToken);
        userDetails.setRefreshToken(newRefreshToken);

        return ApiResponse.success(userDetails);
    }

    /**
     * Gateway가 전달한 내부 인증 헤더의 사용자/sessionId 기준으로 DB refresh token과 Redis 세션을 제거한다.
     *
     * @param userId 로그아웃할 사용자 ID
     * @param sessionId 삭제할 Redis 세션 ID
     * @param type 사용할 로그인 저장소 유형
     * @return 로그아웃 성공 응답
     * @throws BwgAuthException 저장소 유형이 유효하지 않은 경우
     */
    @Override
    @Transactional
    public ApiResponse<Void> logout(String userId, String sessionId, String type) {
        // 로그인 type에 맞는 저장소 선택
        LoginRepository repo = getRepo(type);

        // DB refresh token 제거에 필요한 사용자 식별 정보
        LoginResDto logoutUser = new LoginResDto();
        logoutUser.setUsrId(userId);
        logoutUser.setRefreshToken(null);
        logoutUser.setRefreshTokenExpiresAt(null);

        // 사용자 ID 기준 DB refresh token 제거
        repo.updateRefreshToken(logoutUser);

        // access token claim에서 전달된 sessionId 기준 Redis 세션 삭제
        sessionContextService.deleteBySessionId(sessionId);

        return ApiResponse.success(null);
    }

    /**
     * Spring Security 연동 확장 지점. 현재 인증 흐름은 login/refresh token 저장소 조회가 담당한다.
     *
     * <p>현재 구현은 사용자 조회를 수행하지 않고 항상 {@code null}을 반환한다.</p>
     *
     * @param username 조회할 사용자 이름
     * @return 현재 구현에서는 항상 {@code null}
     */
    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UserNotFoundException {
        return null;
    }

    /**
     * 요청 type 값으로 로그인 저장소 구현체를 선택하고, 유효하지 않은 type은 인증 예외로 변환한다.
     *
     * @param type 선택할 로그인 저장소 유형
     * @return type이 없으면 기본 저장소, 값이 있으면 해당 유형으로 등록된 저장소
     * @throws BwgAuthException type에 해당하는 저장소가 등록되어 있지 않은 경우
     */
    private LoginRepository getRepo(String type) {
        // type이 없으면 기본 저장소, type이 있으면 등록된 저장소 맵에서 선택
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
