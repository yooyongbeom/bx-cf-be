package com.bwg.channel.backend.authsvc.authentication.controller;


import com.bwg.channel.backend.authsvc.authentication.dto.ErpLoginDto;
import com.bwg.channel.backend.authsvc.authentication.dto.LoginReqDto;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticationControllerTest {
    @Test
    void erpLoginJsonTest() throws JsonProcessingException {
        ErpLoginDto dto = new ErpLoginDto();

        ErpLoginDto.Header header = new ErpLoginDto.Header();
        header.setApplication("MyApp");
        header.setService("AuthService");
        header.setOperation("Login");
        header.setTrUuid("1234-5678");

//        Map<String, String> header = new HashMap<String, String>();
//        header.put("application", "MyApp");
//        header.put("service", "AuthService");
//        header.put("operation", "Login");
//        header.put("trUuid", "1234-5678");
//        header.put("userInfo", "");

        ErpLoginDto.Header.UserInfo userInfo = new ErpLoginDto.Header.UserInfo();
        header.setUserInfo(userInfo);

        ErpLoginDto.SSMAUTH00101In rqstData = new ErpLoginDto.SSMAUTH00101In();
        rqstData.setId("yyb1234");
        rqstData.setPassword("test1234");

        dto.setHeader(header);
        dto.setSSMAUTH00101In(rqstData);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);

        System.out.println(json);
    }

    @Test
    void loginRequestBodiesUseApiRequestWrapper() throws NoSuchMethodException {
        assertLoginRequestBody(AuthenticationController.class.getDeclaredMethod(
                "erpLogin",
                ApiRequest.class,
                jakarta.servlet.http.HttpServletResponse.class
        ));
        assertLoginRequestBody(AuthenticationController.class.getDeclaredMethod(
                "login",
                ApiRequest.class,
                jakarta.servlet.http.HttpServletResponse.class
        ));
    }

    private void assertLoginRequestBody(Method method) {
        assertThat(method.getParameters()[0].getAnnotation(RequestBody.class)).isNotNull();
        assertThat(method.getParameterTypes()[0]).isEqualTo(ApiRequest.class);

        Type requestType = method.getGenericParameterTypes()[0];
        assertThat(requestType).isInstanceOf(ParameterizedType.class);
        ParameterizedType parameterizedType = (ParameterizedType) requestType;
        assertThat(parameterizedType.getRawType()).isEqualTo(ApiRequest.class);
        assertThat(parameterizedType.getActualTypeArguments()).containsExactly(LoginReqDto.class);
    }
}
