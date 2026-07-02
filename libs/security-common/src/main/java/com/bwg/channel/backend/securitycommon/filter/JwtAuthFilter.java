package com.bwg.channel.backend.securitycommon.filter;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.securitycommon.constants.AuthErrorCode;
import com.bwg.channel.backend.securitycommon.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    /** Servlet 요청에서 access token을 읽을 Authorization 헤더 이름. */
    private final String X_AUTH_TOKEN = "Authorization";

    /** access token 검증과 인증 객체 생성을 담당하는 JWT 유틸리티. */
    private final JwtUtil jwtUtil;

    /** 인증 실패 응답을 공통 JSON 구조로 직렬화하는 ObjectMapper. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // Authorization 헤더에서 Bearer access token 확인
        String header = request.getHeader(X_AUTH_TOKEN);

        if (header != null && header.startsWith("Bearer ")) {
            // Bearer 접두어를 제외한 순수 JWT 문자열
            String token = header.substring(7);
            try {
                // access token 서명/만료/type claim 검증
                jwtUtil.validateAccessToken(token);

                // 검증된 access token claim으로 Servlet SecurityContext 인증 객체 저장
                Authentication authentication = jwtUtil.getAuthenticationFromToken(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                // 만료된 access token 응답 후 필터 체인 종료
                sendErrorResponse(response, AuthErrorCode.EXPIRED_TOKEN);
                return;
            } catch (JwtException | IllegalArgumentException e) {
                // 서명 오류, 타입 오류 등 유효하지 않은 access token 응답 후 필터 체인 종료
                sendErrorResponse(response, AuthErrorCode.INVALID_TOKEN);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, AuthErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 공통 ApiResponse 실패 구조를 인증 실패 응답으로 직렬화
        ApiResponse<?> apiResponse = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);

        response.getWriter().write(jsonResponse);
    }
}
