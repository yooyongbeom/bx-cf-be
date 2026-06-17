package com.bwg.channel.backend.authsvc.domain.dto;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import lombok.Data;

@Data
@ApiDto(name = "RefreshTkn", endpoints = {"refresh-token"}, generateResponse = false)
public class RefreshTknReqDto {

    @ApiField(description = "리프레시 토큰", required = {"refresh-token"})
    private String refreshToken;
}
