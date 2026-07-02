package com.bwg.channel.backend.gateway.filter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerThemeFilterPackageTest {

    @Test
    void swaggerThemeFilterLivesInFilterPackage() {
        assertThat(SwaggerThemeFilter.class.getPackageName())
                .isEqualTo("com.bwg.channel.backend.gateway.filter");
    }
}
