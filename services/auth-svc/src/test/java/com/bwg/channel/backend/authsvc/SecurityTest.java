package com.bwg.channel.backend.authsvc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.security.SecureRandom;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("local")
@SpringBootTest(properties = {
        "spring.jwt.secret=01234567890123456789012345678901",
        "spring.datasource.jpa-main.jdbc-url=jdbc:h2:mem:auth_jpa;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.mybatis-main.jdbc-url=jdbc:h2:mem:auth_mybatis;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.jpa-main.driver-class-name=org.h2.Driver",
        "spring.datasource.mybatis-main.driver-class-name=org.h2.Driver",
        "spring.datasource.jpa-main.username=sa",
        "spring.datasource.mybatis-main.username=sa",
        "spring.datasource.jpa-main.password=",
        "spring.datasource.mybatis-main.password=",
        "eureka.client.enabled=false",
        "app.log-path=build/logs"
})
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;
//    @Test
//    void accessDeniedTest() throws Exception {
//        mockMvc.perform(get("/err-test2")
//                        .with(user("user").roles("USER"))) // ADMIN 아님
//                .andExpect(status().isForbidden());
//    }

//    @Test
//    void unauthenticatedTest() throws Exception {
//        mockMvc.perform(get("/err-test2"))
//                .andExpect(status().isUnauthorized());
//    }


    @Test
    void encSecretKeyTest() throws Exception {
        final String secKey = getRandomStr();
        System.out.println("Generated 32-char JWT Secret Base 64: " + Base64.getEncoder().encodeToString(secKey.getBytes()));
    }

    String getRandomStr() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()-_=+[]{}|;:,.<>?";

        String allChars = upper + lower + digits + special;
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 32; i++) {
            int index = random.nextInt(allChars.length());
            sb.append(allChars.charAt(index));
        }

        String secretKey = sb.toString();
        System.out.println("Generated 32-char JWT Secret: " + secretKey);

        return secretKey;
    }
}
