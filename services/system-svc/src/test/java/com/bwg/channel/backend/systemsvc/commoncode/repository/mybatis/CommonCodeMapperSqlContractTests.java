package com.bwg.channel.backend.systemsvc.commoncode.repository.mybatis;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 공통코드 교체 SQL이 그룹 단위 DELETE와 단일 배치 INSERT 계약을 지키는지 검증한다.
 */
class CommonCodeMapperSqlContractTests {

    @Test
    void replaceDeletesOnlyTheRequestedGroup() throws Exception {
        String mapperXml = Files.readString(findCommonCodeMapperXml());

        assertThat(mapperXml).contains("<delete id=\"deleteCommonCodesByGroupCd\">");
        assertThat(mapperXml).contains("DELETE FROM common_codes c");
        assertThat(mapperXml).contains("USING common_code_groups g");
        assertThat(mapperXml).contains("g.group_cd = #{groupCd}");
    }

    @Test
    void replaceUsesOneForeachBatchInsertWithNullParent() throws Exception {
        String mapperXml = Files.readString(findCommonCodeMapperXml());

        assertThat(mapperXml).contains("<insert id=\"insertCommonCodes\">");
        assertThat(mapperXml).contains("<foreach collection=\"codes\" item=\"item\" separator=\",\">");
        assertThat(mapperXml).contains("NULL AS parent_code_id");
        assertThat(mapperXml).doesNotContain("${");
    }

    @Test
    void writesSystemFieldsFromExplicitUserParametersAndDatabaseTime() throws Exception {
        String mapperXml = Files.readString(findCommonCodeMapperXml());

        assertThat(mapperXml).doesNotContain("#{data.createdBy}");
        assertThat(mapperXml).contains("#{createdBy}");
        assertThat(mapperXml).contains("#{updatedBy}");
        assertThat(mapperXml).contains("current_timestamp");
    }

    private static Path findCommonCodeMapperXml() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null) {
            Path mapperXml = current.resolve("src/main/resources/mapper/system-svc/CommonCodeMapper.xml");
            if (Files.exists(mapperXml)) {
                return mapperXml;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("CommonCodeMapper.xml not found");
    }
}
