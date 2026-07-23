package com.bwg.channel.backend.systemsvc.referencedata.repository.mybatis;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 기준정보 버전 변경 SQL이 전용 Mapper에만 존재하는지 검증한다.
 */
class ReferenceDataVersionMapperSqlContractTests {

    @Test
    void ownsVersionHistoryInsertAndLatestVersionUpdateStatements() throws Exception {
        String mapperXml = Files.readString(findMapperXml("ReferenceDataVersionMapper.xml"));

        assertThat(mapperXml)
                .contains("<insert id=\"insertReferenceDataVersionHistory\">")
                .contains("INSERT INTO reference_data_version_histories")
                .contains("<update id=\"updateReferenceDataVersion\">")
                .contains("UPDATE reference_data_versions");
    }

    @Test
    void removesVersionChangeStatementsFromFeatureMappers() throws Exception {
        String commonCodeMapperXml = Files.readString(findMapperXml("CommonCodeMapper.xml"));
        String menuMapperXml = Files.readString(findMapperXml("MenuMapper.xml"));

        assertThat(commonCodeMapperXml)
                .doesNotContain("insertReferenceDataVersionHistory")
                .doesNotContain("updateReferenceDataVersion");
        assertThat(menuMapperXml)
                .doesNotContain("insertReferenceDataVersionHistory")
                .doesNotContain("updateReferenceDataVersion");
    }

    /**
     * 실행 위치의 상위 경로를 순회해 system-svc Mapper XML을 찾는다.
     *
     * @param fileName 찾을 Mapper XML 파일명
     * @return 존재하는 Mapper XML 절대 경로
     */
    private static Path findMapperXml(String fileName) {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null) {
            Path mapperXml = current.resolve("src/main/resources/mapper/system-svc").resolve(fileName);
            if (Files.exists(mapperXml)) {
                return mapperXml;
            }
            current = current.getParent();
        }
        throw new IllegalStateException(fileName + " not found");
    }
}
