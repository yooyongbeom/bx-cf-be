//package com.bwg.channel.backend.authsvc.security;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.AuthenticationEntryPoint;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//// 인증 실패 처리
//@Slf4j
//@Component
//public class CustomAuthEntryPoint implements AuthenticationEntryPoint {
//
//    @Override
//    public void commence(HttpServletRequest request,
//                         HttpServletResponse response,
//                         AuthenticationException authException) throws IOException {
//
//        log.warn("Unauthorized access attempt: {}", authException.getMessage());
//
//        response.setContentType("application/json");
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//
//        String json = String.format("{\n"
//                + "    \"status\": %s,\n"
//                + "    \"error\": \"Unauthorized\",\n"
//                + "    \"message\": \"인증이 필요합니다.\",\n"
//                + "    \"path\": \"%s\"\n"
//                + "}", HttpServletResponse.SC_UNAUTHORIZED, request.getRequestURI());
//
//        response.getWriter().write(json);
//    }
//}