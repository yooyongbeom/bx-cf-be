package com.bwg.channel.backend.authsvc.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "로그인 요청/응답 정보")
public class LoginDto {

    @Schema(description = "사용자 ID", example = "hong.gildong")
    private String usrId;

    @Schema(description = "사용자명", example = "홍길동")
    private String usrNm;

    @Schema(description = "직위명", example = "대리")
    private String positDivName;

    @Schema(description = "부서명", example = "채널개발팀")
    private String deptName;

    @Schema(description = "비밀번호")
    private String usrPwd;

    @Schema(description = "액세스 토큰", accessMode = Schema.AccessMode.READ_ONLY)
    private String accessToken;

    @Schema(description = "액세스 토큰 만료 일시", accessMode = Schema.AccessMode.READ_ONLY)
    private String accessTokenExpiresAt;

    @Schema(description = "리프레시 토큰", accessMode = Schema.AccessMode.READ_ONLY)
    private String refreshToken;

    @Schema(description = "리프레시 토큰 만료 일시", accessMode = Schema.AccessMode.READ_ONLY)
    private String refreshTokenExpiresAt;

    @Schema(description = "권한 목록", accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> roles;
}

