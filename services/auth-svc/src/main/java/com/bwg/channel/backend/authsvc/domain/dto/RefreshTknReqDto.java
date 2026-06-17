package com.bwg.channel.backend.authsvc.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "토큰 재발급 요청 정보")
public class RefreshTknReqDto {

    @Schema(description = "리프레시 토큰", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
