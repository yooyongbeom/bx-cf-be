package com.bwg.channel.backend.gateway.config;

import com.bwg.channel.backend.gateway.security.WebFluxCustomAccessDeniedHandler;
import com.bwg.channel.backend.gateway.security.WebFluxCustomAuthEntryPoint;
import com.bwg.channel.backend.securitycommon.filter.WebFluxJwtAuthFilter;
import com.bwg.channel.backend.securitycommon.session.SessionValidator;
import com.bwg.channel.backend.securitycommon.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
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

    /**
     * access token의 sessionId가 유효한 세션인지 확인하는 구현체(Redis 기반).
     * 세션 인프라가 없는 환경에서도 게이트웨이가 뜨도록 Optional로 주입한다.
     */
    private final ObjectProvider<SessionValidator> sessionValidatorProvider;

    /** 세션 존재 검증 활성화 여부. 기본 on (로그아웃 시 access token 즉시 무효화). */
    @Value("${gateway.session-check.enabled:true}")
    private boolean sessionCheckEnabled;

    /** 세션 저장소 조회 실패 시 정책. 기본 fail-closed. valkey HA 미비 환경에서만 true로 임시 완화. */
    @Value("${gateway.session-check.fail-open:false}")
    private boolean sessionCheckFailOpen;

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
            "/integration-svc/v3/api-docs",
            "/mci-svc/v3/api-docs",
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
            "/channel/backend/api/v1/auth/password/reset",
            "/channel/backend/api/v1/integration/github/webhook"
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
                // access token 검증 + 세션 존재 검증 후 내부 서비스용 X-Auth-* 헤더 생성
                .addFilterAt(jwtAuthFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    /**
     * JWT 인증 + 세션 존재 검증 필터를 구성한다.
     *
     * <p>permitAll 화이트리스트는 인가 규칙과 동일한 경로 배열로 매처를 만들어 필터와 공유하므로,
     * 화이트리스트 경로는 필터에서도 토큰/세션 검증을 건너뛴다.</p>
     */
    private WebFluxJwtAuthFilter jwtAuthFilter() {
        // 인가 규칙(permitAll)과 동일한 경로로 필터용 매처 구성 → 두 곳의 화이트리스트가 어긋나지 않는다.
        ServerWebExchangeMatcher permitAllMatcher = ServerWebExchangeMatchers.pathMatchers(PERMIT_URL_ARRAY);
        return new WebFluxJwtAuthFilter(
                jwtUtil,
                permitAllMatcher,
                sessionValidatorProvider.getIfAvailable(),
                sessionCheckEnabled,
                sessionCheckFailOpen
        );
    }
}
