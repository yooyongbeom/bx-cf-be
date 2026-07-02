package com.bwg.channel.backend.common.openapi.typebridge.customizer;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;

@ApiDto(name = "TypeBridgeTest", endpoints = {"create"})
class TypeBridgeTestDto {

    @ApiField(description = "테스트 이름", required = {"create"})
    private String name;
}
