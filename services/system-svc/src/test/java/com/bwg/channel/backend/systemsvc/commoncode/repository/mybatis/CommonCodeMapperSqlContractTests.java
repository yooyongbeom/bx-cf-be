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
    void deleteRemovesCodesBeforeTheGroupThroughSeparateStatements() throws Exception {
        String mapperXml = Files.readString(findCommonCodeMapperXml());

        assertThat(mapperXml).contains("<delete id=\"deleteCommonCodesByGroupCd\">");
        assertThat(mapperXml).contains("<delete id=\"deleteCommonCodeGroup\">");
        assertThat(mapperXml).contains("DELETE FROM common_code_groups");
        assertThat(mapperXml).contains("WHERE group_cd = #{groupCd}");
    }

    @Test
    void writesSystemFieldsFromExplicitUserParametersAndDatabaseTime() throws Exception {
        String mapperXml = Files.readString(findCommonCodeMapperXml());

        assertThat(mapperXml).doesNotContain("#{data.createdBy}");
        assertThat(mapperXml).contains("#{createdBy}");
        assertThat(mapperXml).contains("#{updatedBy}");
        assertThat(mapperXml).contains("current_timestamp");
    }

    @Test
    void listQueriesSelectGroupAndCodeIdentifiers() throws Exception {
        String mapperXml = Files.readString(findCommonCodeMapperXml());
        String groupDetail = selectStatement(mapperXml, "findCommonCodeGroupDetail");
        String groupDetails = selectStatement(mapperXml, "findCommonCodeGroupDetails");
        String codeDetails = selectStatement(mapperXml, "findCommonCodeDetails");

        assertThat(groupDetail).contains("group_id");
        assertThat(groupDetails).contains("group_id");
        assertThat(codeDetails)
                .contains("c.code_id")
                .contains("c.group_id");
    }

    /**
     * Mapper XML에서 지정한 select 구문만 분리해 다른 SQL의 컬럼으로 테스트가 통과하지 않게 한다.
     *
     * @param mapperXml 전체 Mapper XML
     * @param statementId 추출할 select statement ID
     * @return 시작 태그부터 종료 태그까지의 select 구문
     */
    private static String selectStatement(String mapperXml, String statementId) {
        String startTag = "<select id=\"" + statementId + "\"";
        int startIndex = mapperXml.indexOf(startTag);
        int endIndex = mapperXml.indexOf("</select>", startIndex);

        assertThat(startIndex).as("%s select start", statementId).isGreaterThanOrEqualTo(0);
        assertThat(endIndex).as("%s select end", statementId).isGreaterThan(startIndex);
        return mapperXml.substring(startIndex, endIndex);
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
