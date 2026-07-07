package com.bwg.channel.backend.authsvc.domain.dto;

import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 토큰 재발급 흐름에서 쿠키 refresh token을 서비스 계층으로 전달하는 내부 요청 DTO.
 */
@Alias("RefreshTknReqDto")
@Data
public class RefreshTknReqDto {

    /** 서명/type 검증 후 DB 사용자 조회 조건으로 사용하는 refresh token. */
    private String refreshToken;
}
