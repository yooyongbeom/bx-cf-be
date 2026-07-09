package com.bwg.channel.backend.common.openapi;

import com.bwg.channel.backend.common.openapi.customizer.ResponseWrapperSchemaCustomizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommonOpenApiPackageTests {

    @Test
    void commonOpenApiUtilitiesArePublishedFromOpenApiPackage() {
        assertThat(OpenApiSupport.class.getPackageName())
                .isEqualTo("com.bwg.channel.backend.common.openapi");
        assertThat(ResponseWrapperSchemaCustomizer.class.getPackageName())
                .isEqualTo("com.bwg.channel.backend.common.openapi.customizer");
    }
}
