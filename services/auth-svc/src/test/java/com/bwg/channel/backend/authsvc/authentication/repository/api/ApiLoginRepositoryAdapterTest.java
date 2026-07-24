package com.bwg.channel.backend.authsvc.authentication.repository.api;

import com.bwg.channel.backend.authsvc.authentication.dto.ErpLoginDto;
import com.bwg.channel.backend.authsvc.authentication.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.authentication.dto.LoginResDto;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.util.ApiCallUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiLoginRepositoryAdapterTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void findByUsrIdAndUsrPwdRunsErpLoginCallThroughExternalApiCircuitBreaker() {
        ApiCallUtil apiCallUtil = mock(ApiCallUtil.class);
        CircuitBreakerFactory circuitBreakerFactory = mock(CircuitBreakerFactory.class);
        CircuitBreaker circuitBreaker = mock(CircuitBreaker.class);
        ApiLoginRepositoryAdapter adapter = new ApiLoginRepositoryAdapter(
                apiCallUtil,
                new ObjectMapper(),
                circuitBreakerFactory
        );
        ReflectionTestUtils.setField(adapter, "erpLoginUrl", "https://erp.example.com/login");

        String responseBody = """
                {
                  "SSMAUTH00101Out": {
                    "loginRet": {
                      "retVal": "S",
                      "message": "ok",
                      "empName": "Hong Gil Dong",
                      "positDivName": "Manager",
                      "deptName": "Channel"
                    }
                  }
                }
                """;
        when(apiCallUtil.postSync(
                eq("https://erp.example.com/login"),
                any(ErpLoginDto.class),
                any(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(responseBody));
        when(circuitBreakerFactory.create("externalApi")).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(Supplier.class), any(Function.class))).thenAnswer(invocation -> {
            Supplier<ResponseEntity<String>> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        LoginReqDto login = new LoginReqDto();
        login.setUsrId("hong.gildong");
        login.setUsrPwd("password");
        ApiRequest<LoginReqDto> request = new ApiRequest<>();
        request.setData(login);

        LoginResDto result = adapter.findByUsrIdAndUsrPwd(request);

        assertThat(result.getUsrId()).isEqualTo("hong.gildong");
        assertThat(result.getUsrNm()).isEqualTo("Hong Gil Dong");
        verify(circuitBreakerFactory).create("externalApi");
        verify(circuitBreaker).run(any(Supplier.class), any(Function.class));
    }
}
