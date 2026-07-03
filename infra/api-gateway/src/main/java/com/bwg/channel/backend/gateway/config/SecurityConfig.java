package com.bwg.channel.backend.gateway.config;

import com.bwg.channel.backend.gateway.security.WebFluxCustomAccessDeniedHandler;
import com.bwg.channel.backend.gateway.security.WebFluxCustomAuthEntryPoint;
import com.bwg.channel.backend.securitycommon.filter.WebFluxJwtAuthFilter;
import com.bwg.channel.backend.securitycommon.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * API Gateway의 CORS, 인증 예외 경로, JWT WebFlux 보안 필터 체인을 구성한다.
 */
@RequiredArgsConstructor
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(GatewayCorsProperties.class)
public class SecurityConfig {
    /** Gateway로 들어온 access token 검증에 사용할 JWT 유틸리티. */
    private final JwtUtil jwtUtil;
    private final GatewayCorsProperties corsProperties;

    /** 로그인/토큰 재발급/문서처럼 JWT 없이 접근 가능한 인증 전 API 화이트리스트. */
    private static final String[] PERMIT_URL_ARRAY = {
            /* swagger v2 */
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            /* swagger v3 */
            "/v3/api-docs/**",
            "/swagger-ui/**",
            /* 서비스별 api-docs 프록시 경로 */
            "/auth-svc/v3/api-docs",
            "/product-svc/v3/api-docs",
            "/system-svc/v3/api-docs",
            "/swagger-theme.css",
            "/favicon.ico",
            "/actuator/info",
            "/actuator/health",
            "/channel/backend/api/v1/auth/login",
            "/channel/backend/api/v1/auth/erp-login",
            "/channel/backend/api/v1/auth/refresh-token",
            "/channel/backend/api/v1/auth/signup",
            "/channel/backend/api/v1/auth/password/find",
            "/channel/backend/api/v1/auth/password/reset-request",
            "/channel/backend/api/v1/auth/password/reset"
    };

    /**
     * 브라우저 클라이언트 요청에 적용할 CORS 정책을 구성한다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // 환경별 설정에 등록된 프론트엔드 origin만 Gateway CORS 정책으로 허용
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns());
        config.setAllowedMethods(corsProperties.getAllowedMethods());
        config.setAllowedHeaders(corsProperties.getAllowedHeaders());
        config.setExposedHeaders(corsProperties.getExposedHeaders());
        config.setAllowCredentials(corsProperties.getAllowCredentials());
        config.setMaxAge(corsProperties.getMaxAge());

        // Gateway 전체 경로에 동일 CORS 정책 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * JWT 인증을 수행하고 내부 인증 헤더를 생성하는 WebFlux 필터 Bean.
     */
    @Bean
    public WebFluxJwtAuthFilter WebFlux() {
        return new WebFluxJwtAuthFilter(jwtUtil);
    }

    /**
     * permitAll 경로와 JWT 인증 필터를 포함한 Gateway 보안 체인을 구성한다.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // REST API Gateway에서 사용하지 않는 기본 인증 메커니즘 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(auth -> auth
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 로그인/재발급/문서 경로는 JWT 검증 없이 통과
                        .pathMatchers(PERMIT_URL_ARRAY).permitAll()
                        .pathMatchers(HttpMethod.GET, "/te/st.do").permitAll()
                        // 나머지 Gateway 요청은 access token 인증 필수
                        .anyExchange().authenticated()
                )
                .exceptionHandling(ex -> ex
                        // 인증 실패와 권한 부족 응답을 공통 JSON 구조로 변환
                        .authenticationEntryPoint(new WebFluxCustomAuthEntryPoint())
                        .accessDeniedHandler(new WebFluxCustomAccessDeniedHandler())
                )
                // access token 검증 후 내부 서비스용 X-Auth-* 헤더 생성
                .addFilterAt(new WebFluxJwtAuthFilter(jwtUtil), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
