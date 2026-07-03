package com.bwg.channel.backend.common.constants.enums;

import org.springframework.http.HttpStatus;

public interface BwgErrorCode {
    String getCode();
    String getMsg();
    HttpStatus getStatus();
}
