package com.bwg.channel.backend.securitycommon.exception;

// 인터페이스 시그니처를 커스텀 예외로 바꿈
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String msg) { super(msg); }
}
