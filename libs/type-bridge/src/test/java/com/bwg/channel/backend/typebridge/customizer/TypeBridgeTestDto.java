package com.bwg.channel.backend.typebridge.customizer;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;

@ApiDto(name = "TypeBridgeTest", endpoints = {"create"})
class TypeBridgeTestDto {

    @ApiField(description = "테스트 이름", required = {"create"})
    private String name;
}
