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
            "/favicon.ico",
            "/actuator/info",
            "/actuator/health",
            "/channel/backend/api/v1/auth/**"            // 일단 auth 하위로 허용
    };

    @Bean
    public WebFluxJwtAuthFilter WebFlux() {
        return new WebFluxJwtAuthFilter(jwtUtil);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
        http
                // 기본 인증 메커니즘 제거 (rest api용)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)   // Basic Auth 끔
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)   // /login 페이지 끔
                .csrf(ServerHttpSecurity.CsrfSpec::disable)             // csrf 보안이 필요 없음. disable
                .authorizeExchange(auth -> auth
                        .pathMatchers(PERMIT_URL_ARRAY).permitAll()
                        .pathMatchers("GET", "/te/st.do").permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new WebFluxCustomAuthEntryPoint())     // 인증실패 커스텀
                        .accessDeniedHandler(new WebFluxCustomAccessDeniedHandler())     // 권한없음 커스텀
                )
                .addFilterAt(new WebFluxJwtAuthFilter(jwtUtil), SecurityWebFiltersOrder.AUTHENTICATION);
                ;

        return http.build();
    }
}
