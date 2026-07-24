package com.bwg.channel.backend.authsvc.usermanagement.repository.mybatis;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class UserManagementMapperSqlContractTests {

    @Test
    void responsesNeverSelectPasswordsOrRefreshTokens() throws Exception {
        String xml = Files.readString(findMapperXml());
        String listSql = statement(xml, "select", "findUsers");
        String detailSql = statement(xml, "select", "findUser");

        assertThat(listSql + detailSql)
                .doesNotContain("USER_PWD")
                .doesNotContain("REFRESH_TOKEN");
    }

    @Test
    void createUsesTrustedSystemFieldsAndRoleNameLookup() throws Exception {
        String xml = Files.readString(findMapperXml());

        assertThat(statement(xml, "insert", "insertUser"))
                .contains("#{createdBy}")
                .contains("current_timestamp")
                .doesNotContain("#{request.data.createdBy}");
        assertThat(statement(xml, "select", "findRoleIdsByName"))
                .contains("FROM ROLES")
                .contains("ROLE_NAME = #{roleName}");
        assertThat(statement(xml, "insert", "insertUserRole"))
                .contains("INSERT INTO USER_ROLES")
                .contains("USER_ID")
                .contains("ROLE_ID")
                .contains("#{userId}")
                .contains("#{roleId}")
                // 현재 USER_ROLES 스키마 계약은 사용자와 역할 FK 두 열뿐이다.
                .doesNotContain("CREATED_BY")
                .doesNotContain("CREATED_AT")
                .doesNotContain("UPDATED_BY")
                .doesNotContain("UPDATED_AT")
                .doesNotContain("#{createdBy}")
                .doesNotContain("current_timestamp");
    }

    @Test
    void updateLeavesCredentialsAndRolesUntouched() throws Exception {
        String updateSql = statement(Files.readString(findMapperXml()), "update", "updateUser");

        assertThat(updateSql)
                .contains("USER_NM = #{request.data.usrNm}")
                .contains("UPDATED_BY = #{updatedBy}")
                .doesNotContain("USER_PWD")
                .doesNotContain("USER_ROLES")
                .doesNotContain("REFRESH_TOKEN");
    }

    @Test
    void deleteUsesSeparateRoleAndUserStatements() throws Exception {
        String xml = Files.readString(findMapperXml());

        assertThat(statement(xml, "delete", "deleteUserRoles"))
                .contains("DELETE FROM USER_ROLES")
                .contains("USER_ID = #{userId}");
        assertThat(statement(xml, "delete", "deleteUser"))
                .contains("DELETE FROM USERS")
                .contains("USER_ID = #{userId}");
    }

    private static String statement(String xml, String tag, String id) {
        String start = "<" + tag + " id=\"" + id + "\"";
        int startIndex = xml.indexOf(start);
        int endIndex = xml.indexOf("</" + tag + ">", startIndex);
        assertThat(startIndex).as("%s start", id).isGreaterThanOrEqualTo(0);
        assertThat(endIndex).as("%s end", id).isGreaterThan(startIndex);
        return xml.substring(startIndex, endIndex);
    }

    private static Path findMapperXml() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null) {
            Path xml = current.resolve("src/main/resources/mapper/auth-svc/UserManagementMapper.xml");
            if (Files.exists(xml)) {
                return xml;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("UserManagementMapper.xml not found");
    }
}
