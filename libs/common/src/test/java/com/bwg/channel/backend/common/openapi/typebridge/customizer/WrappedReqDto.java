package com.bwg.channel.backend.common.openapi.typebridge.customizer;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;

@ApiDto(type = ApiType.REQUEST, name = "Wrapped", endpoints = {"login"})
class WrappedReqDto {

    @ApiField(description = "User ID", example = "sample-user", required = {"login"})
    private String usrId;

    @ApiField(description = "Password", format = "password", required = {"login"})
    private String usrPwd;
}
