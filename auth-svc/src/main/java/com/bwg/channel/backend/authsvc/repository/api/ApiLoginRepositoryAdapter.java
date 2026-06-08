package com.bwg.channel.backend.authsvc.repository.api;

import com.bwg.channel.backend.authsvc.domain.dto.ErpLoginDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.repository.LoginRepository;
import com.bwg.channel.backend.common.constants.enums.AuthErrorCode;
import com.bwg.channel.backend.common.exception.BwgAuthException;
import com.bwg.channel.backend.common.util.ApiCallUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component("apiLogin")
@RequiredArgsConstructor
@Primary
public class ApiLoginRepositoryAdapter implements LoginRepository {
    private final ApiCallUtil apiCallUtil;
    private final ObjectMapper objectMapper;

    @Value("${bwgProp.erp-login-url}")
    private String erpLoginUrl;

    @Override
    public LoginDto findByUsrIdAndUsrPwd(LoginDto paramDto) {
        ErpLoginDto dto = makeErpDto(paramDto);

        final String userAgent = "Mozila/5.0";
        final String contentType = "application/json";

        Map<String, String> header = new HashMap<>();
        header.put("User-Agent", userAgent);
        header.put("Content-type", contentType);

        ResponseEntity<String> response = apiCallUtil.postSync(erpLoginUrl, dto, header, new ParameterizedTypeReference<String>() {});

        log.info("response code : {}", response.getStatusCode());
        log.info("response : {}", response);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            try {
                Map<String, Object> result = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
                return makeResult(paramDto.getUsrId(), result);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
                Map<String, Object> details = new HashMap<>();
                details.put("param", dto);
                throw new BwgAuthException.Builder()
                        .code(AuthErrorCode.JSON_STR_TO_VO_PARSING_ERROR)
                        .message(AuthErrorCode.JSON_STR_TO_VO_PARSING_ERROR.getMsg())
                        .details(Collections.unmodifiableMap(details))
                        .build();
            }
        } else {
            throw new BwgAuthException.Builder()
                    .code(AuthErrorCode.SERVER_ERROR)
                    .message("responseStatusCode : " + response.getStatusCode() + " responseBody : " + response.getBody())
                    .build();
        }
    }

    // API return null
    @Override
    public LoginDto findByRefreshToken(RefreshTknReqDto refreshTknReqDto) {
        return null;
    }

    @Override
    public int updateRefreshToken(LoginDto loginDto) {
        log.warn("updateRefreshToken is not implemented for API login strategy.");
        return 0;
    }

    @SuppressWarnings("unchecked")
    private LoginDto makeResult(String usrId, Map<String, Object> rsltMap) {
        Object outObj = rsltMap.get("SSMAUTH00101Out");
        if (outObj instanceof Map) {
            Map<String, Object> outMap = (Map<String, Object>) outObj;
            Object loginRetObj = outMap.get("loginRet");
            if (loginRetObj instanceof Map) {
                Map<String, Object> loginRet = (Map<String, Object>) loginRetObj;
                if (!"S".equals(loginRet.get("retVal"))) {
                    throw new BwgAuthException.Builder()
                            .code(AuthErrorCode.UNAUTHORIZED_CLIENT)
                            .message(loginRet.get("message").toString())
                            .build();
                }
                else {
                    LoginDto loginDto = new LoginDto();
                    // role은 추후에 정의
                    //List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
                    loginDto.setUsrId(usrId);
                    loginDto.setUsrNm(loginRet.get("empName").toString());
                    loginDto.setPositDivName(loginRet.get("positDivName").toString());
                    loginDto.setDeptName(loginRet.get("deptName").toString());
                    //loginDto.setRoles(roles);
                    loginDto.setRoles(null);
                    return loginDto;
                }
            }
        }
        throw new BwgAuthException.Builder()
                .code(AuthErrorCode.SERVER_ERROR)
                .message("Failed to parse API login response")
                .build();
    }

    private ErpLoginDto makeErpDto(LoginDto paramDto) {
        final String grwUUID = UUID.randomUUID().toString().replace("-", "");

        ErpLoginDto dto = new ErpLoginDto();

        ErpLoginDto.Header header = new ErpLoginDto.Header();
        header.setApplication("SM-onl");
        header.setService("SSMAUTH001");
        header.setOperation("SSMAUTH00101");
        header.setTrUuid("grwUUID");

        ErpLoginDto.Header.UserInfo userInfo = new ErpLoginDto.Header.UserInfo();
        header.setUserInfo(userInfo);

        ErpLoginDto.SSMAUTH00101In rqstData = new ErpLoginDto.SSMAUTH00101In();
        rqstData.setId(paramDto.getUsrId());
        rqstData.setPassword(paramDto.getUsrPwd());

        dto.setHeader(header);
        dto.setSSMAUTH00101In(rqstData);

        return dto;
    }
}