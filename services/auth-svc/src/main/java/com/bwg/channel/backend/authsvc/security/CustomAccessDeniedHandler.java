//package com.bwg.channel.backend.authsvc.security;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.web.access.AccessDeniedHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//// 권한 없음 처리
//@Slf4j
//@Component
//public class CustomAccessDeniedHandler implements AccessDeniedHandler {
//
//    @Override
//    public void handle(HttpServletRequest request,
//                       HttpServletResponse response,
//                       AccessDeniedException accessDeniedException) throws IOException {
//
//        log.warn("Forbidden access attempt: {}", accessDeniedException.getMessage());
//
//        response.setContentType("application/json");
//        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//
//        String json = String.format("{\n"
//                + "    \"status\": %s,\n"
//                + "    \"error\": \"Unauthorized\",\n"
//                + "    \"message\": \"권한이 없습니다.\",\n"
//                + "    \"path\": \"%s\"\n"
//                + "}", HttpServletResponse.SC_FORBIDDEN, request.getRequestURI());
//
//        response.getWriter().write(json);
//    }
//}