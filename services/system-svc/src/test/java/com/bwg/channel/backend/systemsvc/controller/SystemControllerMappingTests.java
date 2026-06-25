package com.bwg.channel.backend.systemsvc.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 기준정보 컨트롤러 POST 매핑 규칙 검증
 */
class SystemControllerMappingTests {

    @Test
    void commonCodeControllerUsesPostMappingsOnly() {
        assertPostOnly(CommonCodeController.class);
    }

    @Test
    void menuControllerUsesPostMappingsOnly() {
        assertPostOnly(MenuController.class);
    }

    private void assertPostOnly(Class<?> controllerType) {
        for (Method method : controllerType.getDeclaredMethods()) {
            assertThat(method.isAnnotationPresent(GetMapping.class))
                    .as("%s.%s must not use GET", controllerType.getSimpleName(), method.getName())
                    .isFalse();
            assertThat(method.isAnnotationPresent(PutMapping.class))
                    .as("%s.%s must not use PUT", controllerType.getSimpleName(), method.getName())
                    .isFalse();
            assertThat(method.isAnnotationPresent(PostMapping.class))
                    .as("%s.%s must use POST", controllerType.getSimpleName(), method.getName())
                    .isTrue();
        }
    }
}
