package com.bwg.channel.backend.authsvc.controller;


import com.bwg.channel.backend.authsvc.domain.dto.ErpLoginDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

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
}