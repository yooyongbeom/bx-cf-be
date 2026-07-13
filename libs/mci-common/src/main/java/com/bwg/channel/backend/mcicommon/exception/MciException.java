package com.bwg.channel.backend.mcicommon.exception;

/**
 * MCI 라우팅/변환/adapter 선택 과정에서 발생하는 공통 예외.
 */
public class MciException extends RuntimeException {

    public MciException(String message) {
        super(message);
    }
}
