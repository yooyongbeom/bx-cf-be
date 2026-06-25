package com.bwg.channel.backend.authsvc.domain.dto;

import lombok.Data;

/**
 * refresh token 재발급 처리에 사용하는 내부 요청 DTO.
 */
@Data
public class RefreshTknReqDto {

    private String refreshToken;
}
