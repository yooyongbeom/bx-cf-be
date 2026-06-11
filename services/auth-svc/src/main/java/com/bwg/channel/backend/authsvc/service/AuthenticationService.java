package com.bwg.channel.backend.authsvc.service;

import com.bwg.channel.backend.authsvc.domain.dto.LoginDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;

public interface AuthenticationService {
    ApiResponse<LoginDto> login(LoginDto paramDto, String type);

    ApiResponse<LoginDto> refreshToken(RefreshTknReqDto paramDto, String type);
}
