package com.bwg.channel.backend.sessioncontext.service;

import com.bwg.channel.backend.sessioncontext.repository.SessionContextRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultSessionContextServiceTest {

    @Test
    void delegatesOperationOwnedSessionCreationBlock() {
        SessionContextRepository repository = mock(SessionContextRepository.class);
        DefaultSessionContextService service = new DefaultSessionContextService(repository);
        when(repository.blockSessionCreation("target-user", "delete-operation-1")).thenReturn(true);

        boolean acquired = service.blockSessionCreation("target-user", "delete-operation-1");

        assertThat(acquired).isTrue();
        // 업무 계층은 Redis SET NX 구현을 몰라도 작업 토큰 소유권 획득 결과를 사용할 수 있어야 한다.
        verify(repository).blockSessionCreation("target-user", "delete-operation-1");
    }

    @Test
    void delegatesOperationOwnedSessionCreationUnblock() {
        SessionContextRepository repository = mock(SessionContextRepository.class);
        DefaultSessionContextService service = new DefaultSessionContextService(repository);

        service.unblockSessionCreation("target-user", "delete-operation-1");

        // 보상 제거에는 최초 차단 획득 시 사용한 동일 작업 토큰을 전달한다.
        verify(repository).unblockSessionCreation("target-user", "delete-operation-1");
    }

    @Test
    void delegatesSessionCreationBlockedLookup() {
        SessionContextRepository repository = mock(SessionContextRepository.class);
        DefaultSessionContextService service = new DefaultSessionContextService(repository);
        when(repository.isSessionCreationBlocked("target-user")).thenReturn(true);

        assertThat(service.isSessionCreationBlocked("target-user")).isTrue();

        verify(repository).isSessionCreationBlocked("target-user");
    }
}
