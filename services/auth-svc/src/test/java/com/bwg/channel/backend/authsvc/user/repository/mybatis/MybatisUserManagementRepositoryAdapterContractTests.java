package com.bwg.channel.backend.authsvc.user.repository.mybatis;

import com.bwg.channel.backend.authsvc.user.repository.mybatis.mapper.UserManagementMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MybatisUserManagementRepositoryAdapterContractTests {

    @Test
    void insertsUserRoleWithTrustedSystemActor() {
        UserManagementMapper mapper = mock(UserManagementMapper.class);
        MybatisUserManagementRepositoryAdapter adapter =
                new MybatisUserManagementRepositoryAdapter(mapper);
        when(mapper.insertUserRole("user-1", 7L, "admin")).thenReturn(1);

        int affectedRows = adapter.insertUserRole("user-1", 7L, "admin");

        assertThat(affectedRows).isEqualTo(1);
        // Gateway가 검증한 작업자를 USER_ROLES 시스템 필드 생성에 사용하도록 Mapper까지 전달한다.
        verify(mapper).insertUserRole("user-1", 7L, "admin");
    }
}
