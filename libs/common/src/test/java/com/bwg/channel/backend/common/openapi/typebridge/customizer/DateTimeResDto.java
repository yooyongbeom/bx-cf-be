package com.bwg.channel.backend.common.openapi.typebridge.customizer;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;

import java.time.OffsetDateTime;

@ApiDto(type = ApiType.RESPONSE, name = "DateTime", endpoints = {"detail"})
class DateTimeResDto {

    @ApiField(description = "Changed at", required = {"detail"})
    private OffsetDateTime changedAt;
}
