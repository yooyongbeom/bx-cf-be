package com.bwg.channel.backend.sessioncontext.exception;

/**
 * 사용자 삭제 tombstone으로 인해 신규 세션 생성을 거부할 때 발생하는 예외.
 */
public class SessionCreationBlockedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SessionCreationBlockedException(String userId) {
        // 외부 응답에서는 인증 거부로 변환되며, 이 메시지는 내부 원인 식별에만 사용한다.
        super("Session creation is blocked for user: " + userId);
    }
}
