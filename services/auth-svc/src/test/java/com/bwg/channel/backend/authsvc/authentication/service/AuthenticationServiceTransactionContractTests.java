package com.bwg.channel.backend.authsvc.authentication.service;

import com.bwg.channel.backend.authsvc.authentication.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.authentication.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.authentication.repository.mybatis.MybatisLoginRepositoryAdapter;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationServiceTransactionContractTests {

    @Test
    void mybatisAuthenticationMethodsUseTheMainMybatisTransactionManager() throws Exception {
        assertTransactionManager(AuthenticationServiceImpl.class, "login", ApiRequest.class, String.class);
        assertTransactionManager(
                AuthenticationServiceImpl.class,
                "refreshToken",
                RefreshTknReqDto.class,
                String.class
        );
        assertTransactionManager(
                AuthenticationServiceImpl.class,
                "logout",
                String.class,
                String.class,
                String.class
        );
    }

    @Test
    void mybatisRefreshTokenUpdateUsesTheMainMybatisTransactionManager() throws Exception {
        assertTransactionManager(
                MybatisLoginRepositoryAdapter.class,
                "updateRefreshToken",
                LoginResDto.class
        );
    }

    private void assertTransactionManager(
            Class<?> ownerType,
            String methodName,
            Class<?>... parameterTypes
    ) throws Exception {
        Method method = ownerType.getDeclaredMethod(methodName, parameterTypes);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional)
                .as("%s.%s must declare @Transactional", ownerType.getSimpleName(), methodName)
                .isNotNull();
        // 인증 orchestration과 MyBatis refresh-token write는 동일한 주 트랜잭션 관리자를 사용한다.
        assertThat(transactional.transactionManager())
                .isEqualTo("mybatisMainTransactionManager");
    }
}
