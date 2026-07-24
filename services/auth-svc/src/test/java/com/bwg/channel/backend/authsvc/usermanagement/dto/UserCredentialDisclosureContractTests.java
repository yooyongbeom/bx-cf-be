package com.bwg.channel.backend.authsvc.usermanagement.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserCredentialDisclosureContractTests {

    @Test
    void createRequestDoesNotDeclareToStringThatCouldExposePlaintextPassword() {
        assertThat(UserCreateReqDto.class.getDeclaredMethods())
                .extracting(Method::getName)
                .doesNotContain("toString");
    }

    @Test
    void authProfilesDisableP6SpyBoundValueLogging() throws Exception {
        for (String profile : List.of("local", "dev")) {
            String yaml = Files.readString(findAuthProfile(profile));

            // 인증 서비스에서는 바인딩된 평문 비밀번호가 SQL 로그로 유출되지 않아야 한다.
            assertThat(yaml)
                    .as("%s auth profile", profile)
                    .containsPattern("(?m)^\\s{4}p6spy:\\s*OFF\\s*$");
        }
    }

    private static Path findAuthProfile(String profile) {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null) {
            Path yaml = current.resolve(
                    "src/main/resources/config/auth-svc/" + profile + "/application-" + profile + ".yml"
            );
            if (Files.exists(yaml)) {
                return yaml;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("auth-svc profile not found: " + profile);
    }
}
