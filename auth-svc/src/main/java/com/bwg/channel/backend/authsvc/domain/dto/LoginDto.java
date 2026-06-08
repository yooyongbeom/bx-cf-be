package com.bwg.channel.backend.authsvc.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginDto {
    private String usrId;
    private String usrNm;
    private String positDivName;
    private String deptName;
    private String usrPwd;
    private String accessToken;
    private String accessTokenExpiresAt;
    private String refreshToken;
    private String refreshTokenExpiresAt;
    private List<String> roles;
}

