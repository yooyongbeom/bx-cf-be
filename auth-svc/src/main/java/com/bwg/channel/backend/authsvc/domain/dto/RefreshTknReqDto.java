package com.bwg.channel.backend.authsvc.domain.dto;

import lombok.Data;

@Data
public class RefreshTknReqDto {
    private String refreshToken;
}
