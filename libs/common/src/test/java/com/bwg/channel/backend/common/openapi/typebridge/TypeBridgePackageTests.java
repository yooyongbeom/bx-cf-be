package com.bwg.channel.backend.common.openapi.typebridge;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import com.bwg.channel.backend.common.openapi.typebridge.customizer.TypeBridgeOpenApiCustomizer;
import com.bwg.channel.backend.common.openapi.typebridge.customizer.TypeBridgeOperationCustomizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypeBridgePackageTests {

    @Test
    void typeBridgeApiIsPublishedFromCommonOpenApiPackage() {
        assertThat(ApiDto.class.getPackageName())
                .isEqualTo("com.bwg.channel.backend.common.openapi.typebridge.annotation");
        assertThat(ApiField.class.getPackageName())
                .isEqualTo("com.bwg.channel.backend.common.openapi.typebridge.annotation");
        assertThat(ApiType.class.getPackageName())
                .isEqualTo("com.bwg.channel.backend.common.openapi.typebridge.annotation");
        assertThat(TypeBridgeOpenApiCustomizer.class.getPackageName())
                .isEqualTo("com.bwg.channel.backend.common.openapi.typebridge.customizer");
        assertThat(TypeBridgeOperationCustomizer.class.getPackageName())
                .isEqualTo("com.bwg.channel.backend.common.openapi.typebridge.customizer");
    }
}
