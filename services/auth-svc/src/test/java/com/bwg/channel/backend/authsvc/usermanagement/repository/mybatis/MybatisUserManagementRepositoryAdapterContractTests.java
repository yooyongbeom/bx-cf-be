package com.bwg.channel.backend.authsvc.usermanagement.repository.mybatis;

import com.bwg.channel.backend.authsvc.usermanagement.repository.mybatis.mapper.UserManagementMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MybatisUserManagementRepositoryAdapterContractTests {

    @Test
    void insertsUserRoleWithOnlyUserAndRoleKeys() {
        UserManagementMapper mapper = mock(UserManagementMapper.class);
        MybatisUserManagementRepositoryAdapter adapter =
                new MybatisUserManagementRepositoryAdapter(mapper);
        when(mapper.insertUserRole("user-1", 7L)).thenReturn(1);

        int affectedRows = adapter.insertUserRole("user-1", 7L);

        assertThat(affectedRows).isEqualTo(1);
        // USER_ROLES에는 감사 열이 없으므로 신뢰된 작업자도 이 저장 계약에는 전달하지 않는다.
        verify(mapper).insertUserRole("user-1", 7L);
    }
}
