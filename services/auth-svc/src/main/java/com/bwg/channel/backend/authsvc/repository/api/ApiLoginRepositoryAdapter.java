package com.bwg.channel.backend.authsvc.repository.api;

import com.bwg.channel.backend.authsvc.domain.dto.ErpLoginDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.repository.LoginRepository;
import com.bwg.channel.backend.common.constants.enums.CommonErrorCode;
import com.bwg.channel.backend.common.util.ApiCallUtil;
import com.bwg.channel.backend.securitycommon.constants.AuthErrorCode;
import com.bwg.channel.backend.securitycommon.exception.BwgAuthException;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component("apiLogin")
@RequiredArgsConstructor
@Primary
public class ApiLoginRepositoryAdapter implements LoginRepository {
    /** ERP 로그인 API 호출을 담당하는 공통 HTTP 유틸리티. */
    private final ApiCallUtil apiCallUtil;

    /** ERP 응답 JSON 문자열을 Map 구조로 변환하는 ObjectMapper. */
    private final ObjectMapper objectMapper;

    /** ERP 인증 API 엔드포인트 URL. */
    @Value("${app.erp-login-url}")
    private String erpLoginUrl;

    @Override
    public LoginResDto findByUsrIdAndUsrPwd(LoginReqDto paramDto) {
        // 로그인 요청 값을 ERP 인증 API 요청 전문으로 변환
        ErpLoginDto dto = makeErpDto(paramDto);

        // ERP API 호출에 필요한 기본 HTTP 헤더
        final String userAgent = "Mozila/5.0";
        final String contentType = "application/json";

        Map<String, String> header = new HashMap<>();
        header.put("User-Agent", userAgent);
        header.put("Content-type", contentType);

        // ERP 인증 API 호출
        ResponseEntity<String> response = apiCallUtil.postSync(erpLoginUrl, dto, header, new ParameterizedTypeReference<String>() {});

        log.info("response code : {}", response.getStatusCode());
        log.info("response : {}", response);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            try {
                // ERP 응답 JSON을 Map으로 파싱 후 로그인 결과 DTO로 변환
                Map<String, Object> result = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
                return makeResult(paramDto.getUsrId(), result);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
                Map<String, Object> details = new HashMap<>();
                details.put("param", dto);
                throw new BwgAuthException.Builder()
                        .code(CommonErrorCode.JSON_STR_TO_VO_PARSING_ERROR)
                        .message(CommonErrorCode.JSON_STR_TO_VO_PARSING_ERROR.getMsg())
                        .details(Collections.unmodifiableMap(details))
                        .build();
            }
        } else {
            throw new BwgAuthException.Builder()
                    .code(CommonErrorCode.SERVER_ERROR)
                    .message("responseStatusCode : " + response.getStatusCode() + " responseBody : " + response.getBody())
                    .build();
        }
    }

    // ERP 로그인 전략은 현재 refresh token 기반 사용자 재조회 흐름을 사용하지 않음
    @Override
    public LoginResDto findByRefreshToken(RefreshTknReqDto refreshTknReqDto) {
        return null;
    }

    @Override
    public int updateRefreshToken(LoginResDto loginResDto) {
        log.warn("updateRefreshToken is not implemented for API login strategy.");
        return 0;
    }

    @SuppressWarnings("unchecked")
    private LoginResDto makeResult(String usrId, Map<String, Object> rsltMap) {
        // ERP 응답의 최상위 출력 전문 추출
        Object outObj = rsltMap.get("SSMAUTH00101Out");
        if (outObj instanceof Map) {
            Map<String, Object> outMap = (Map<String, Object>) outObj;

            // ERP 로그인 결과 블록 추출
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
                    // ERP 인증 성공 응답을 공통 로그인 응답 DTO로 매핑
                    LoginResDto loginResDto = new LoginResDto();
                    // ERP 권한 매핑 기준이 정해지면 roles claim 값으로 반영
                    loginResDto.setUsrId(usrId);
                    loginResDto.setUsrNm(loginRet.get("empName").toString());
                    loginResDto.setPositDivName(loginRet.get("positDivName").toString());
                    loginResDto.setDeptName(loginRet.get("deptName").toString());
                    loginResDto.setRoles(null);
                    return loginResDto;
                }
            }
        }
        throw new BwgAuthException.Builder()
                .code(CommonErrorCode.SERVER_ERROR)
                .message("Failed to parse API login response")
                .build();
    }

    private ErpLoginDto makeErpDto(LoginReqDto paramDto) {
        // ERP 요청 전문 추적용 거래 UUID
        final String grwUUID = UUID.randomUUID().toString().replace("-", "");

        // ERP 인증 API 요청 전문 루트 객체
        ErpLoginDto dto = new ErpLoginDto();

        // ERP 서비스/오퍼레이션 식별 헤더
        ErpLoginDto.Header header = new ErpLoginDto.Header();
        header.setApplication("SM-onl");
        header.setService("SSMAUTH001");
        header.setOperation("SSMAUTH00101");
        header.setTrUuid(grwUUID);

        // ERP 헤더의 사용자 부가 정보 영역
        ErpLoginDto.Header.UserInfo userInfo = new ErpLoginDto.Header.UserInfo();
        header.setUserInfo(userInfo);

        // 로그인 요청 ID/PW를 ERP 입력 전문에 매핑
        ErpLoginDto.SSMAUTH00101In rqstData = new ErpLoginDto.SSMAUTH00101In();
        rqstData.setId(paramDto.getUsrId());
        rqstData.setPassword(paramDto.getUsrPwd());

        dto.setHeader(header);
        dto.setSSMAUTH00101In(rqstData);

        return dto;
    }
}
