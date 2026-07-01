package com.bwg.channel.backend.gateway.cmm.configuration;

import com.bwg.channel.backend.authcore.filter.WebFluxJwtAuthFilter;
import com.bwg.channel.backend.authcore.util.JwtUtil;
import com.bwg.channel.backend.gateway.cmm.security.WebFluxCustomAccessDeniedHandler;
import com.bwg.channel.backend.gateway.cmm.security.WebFluxCustomAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * API Gateway의 CORS, 인증 예외 경로, JWT WebFlux 보안 필터 체인을 설정한다.
 */
@RequiredArgsConstructor
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private final JwtUtil jwtUtil;

    // 허용 url 주로 static
    private static final String[] PERMIT_URL_ARRAY = {
            /*swagger v2*/
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            /*swagger v3*/
            "/v3/api-docs/**",
            "/swagger-ui/**",
            /*swagger 집계: 서비스별 api-docs 프록시 경로*/
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
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * JWT 인증을 수행하는 WebFlux 필터 Bean을 생성한다.
     */
    @Bean
    public WebFluxJwtAuthFilter WebFlux() {
        return new WebFluxJwtAuthFilter(jwtUtil);
    }

    /**
     * permitAll 경로와 JWT 인증 필터를 포함한 Gateway 보안 체인을 구성한다.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 기본 인증 메커니즘 제거 (rest api용)
                .httpBasic(httpBasic -> httpBasic.disable())    // Basic Auth 끔
                .formLogin(form -> form.disable())              // /login 페이지 끔
                .csrf(csrf -> csrf.disable())                   // csrf 보안이 필요 없음. disable
                .authorizeExchange(auth -> auth
                        .pathMatchers(PERMIT_URL_ARRAY).permitAll()
                        .pathMatchers("GET", "/te/st.do").permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new WebFluxCustomAuthEntryPoint())     // 인증실패 커스텀
                        .accessDeniedHandler(new WebFluxCustomAccessDeniedHandler())     // 권한없음 커스텀
                )
                .addFilterAt(new WebFluxJwtAuthFilter(jwtUtil), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
