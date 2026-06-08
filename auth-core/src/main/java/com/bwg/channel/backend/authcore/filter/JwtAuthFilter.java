package com.bwg.channel.backend.authcore.filter;

import com.bwg.channel.backend.authcore.util.JwtUtil;
import com.bwg.channel.backend.common.constants.enums.AuthErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final String X_AUTH_TOKEN = "Authorization";
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(X_AUTH_TOKEN);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                jwtUtil.validateToken(token); // 토큰 유효성 검증
                Authentication authentication = jwtUtil.getAuthenticationFromToken(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                // 토큰 만료 시
                sendErrorResponse(response, AuthErrorCode.EXPIRED_TOKEN);
                return; // 필터 체인 종료
            } catch (JwtException | IllegalArgumentException e) {
                // 그 외 유효하지 않은 토큰
                sendErrorResponse(response, AuthErrorCode.INVALID_TOKEN);
                return; // 필터 체인 종료
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, AuthErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<?> apiResponse = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);

        response.getWriter().write(jsonResponse);
    }
}
