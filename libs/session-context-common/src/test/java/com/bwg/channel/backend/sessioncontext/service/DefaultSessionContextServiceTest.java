package com.bwg.channel.backend.sessioncontext.service;

import com.bwg.channel.backend.sessioncontext.repository.SessionContextRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultSessionContextServiceTest {

    @Test
    void delegatesSessionCreationBlockAndUnblockByUserId() {
        SessionContextRepository repository = mock(SessionContextRepository.class);
        DefaultSessionContextService service = new DefaultSessionContextService(repository);

        service.blockSessionCreation("target-user");
        service.unblockSessionCreation("target-user");

        // 업무 계층은 Redis tombstone 저장 방식을 몰라도 사용자 단위 차단을 사용할 수 있어야 한다.
        verify(repository).blockSessionCreation("target-user");
        verify(repository).unblockSessionCreation("target-user");
    }
}
