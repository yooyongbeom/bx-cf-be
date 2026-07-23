package com.bwg.channel.backend.productsvc.repository.mybatis;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperSqlContractTests {

    @Test
    void productListSortUsesWhitelistBranches() throws Exception {
        String mapperXml = Files.readString(findProductMapperXml());

        assertThat(mapperXml).contains("sort.sort == 'productId,asc'");
        assertThat(mapperXml).contains("sort.sort == 'productId,desc'");
        assertThat(mapperXml).contains("sort.sort == 'productNm,asc'");
        assertThat(mapperXml).contains("sort.sort == 'productNm,desc'");
        assertThat(mapperXml).contains("sort.sort == 'price,asc'");
        assertThat(mapperXml).contains("sort.sort == 'price,desc'");
        assertThat(mapperXml).contains("sort.sort == 'stockQty,asc'");
        assertThat(mapperXml).contains("sort.sort == 'stockQty,desc'");
        assertThat(mapperXml).doesNotContain("${sort}");
    }

    @Test
    void productListUsesSeparatedRequestBlocks() throws Exception {
        String mapperXml = Files.readString(findProductMapperXml());

        assertThat(mapperXml).contains("filter.useYn");
        assertThat(mapperXml).contains("filter.keyword");
        assertThat(mapperXml).contains("filter.searchType");
        assertThat(mapperXml).contains("data.productNm");
        assertThat(mapperXml).contains("pagination.size");
        assertThat(mapperXml).contains("pagination.offset");
    }

    @Test
    void productCountUsesTheSameFiltersWithoutPagingOrSorting() throws Exception {
        String mapperXml = Files.readString(findProductMapperXml());
        String listStatement = xmlElement(mapperXml, "select", "findAll");
        String countStatement = xmlElement(mapperXml, "select", "countAll");
        String filterConditions = xmlElement(mapperXml, "sql", "productSearchConditions");

        assertThat(listStatement).contains("<include refid=\"productSearchConditions\"/>");
        assertThat(countStatement).contains("<include refid=\"productSearchConditions\"/>");
        assertThat(filterConditions)
                .contains("filter.useYn")
                .contains("data.productNm")
                .contains("filter.keyword")
                .contains("filter.searchType");
        assertThat(countStatement)
                .contains("COUNT(*)")
                .doesNotContain("ORDER BY")
                .doesNotContain("LIMIT")
                .doesNotContain("OFFSET");
    }

    /**
     * Mapper XML에서 지정한 요소만 분리해 다른 SQL이나 공통 조각의 문자열로 계약이 통과하지 않게 한다.
     *
     * @param mapperXml 전체 Mapper XML
     * @param elementName 추출할 XML 요소명
     * @param elementId 추출할 요소 ID
     * @return 시작 태그부터 종료 태그까지의 XML 요소
     */
    private static String xmlElement(String mapperXml, String elementName, String elementId) {
        String startTag = "<" + elementName + " id=\"" + elementId + "\"";
        int startIndex = mapperXml.indexOf(startTag);
        int endIndex = mapperXml.indexOf("</" + elementName + ">", startIndex);

        assertThat(startIndex).as("%s start", elementId).isGreaterThanOrEqualTo(0);
        assertThat(endIndex).as("%s end", elementId).isGreaterThan(startIndex);
        return mapperXml.substring(startIndex, endIndex);
    }

    private static Path findProductMapperXml() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null) {
            Path mapperXml = current.resolve("src/main/resources/mapper/product-svc/ProductMapper.xml");
            if (Files.exists(mapperXml)) {
                return mapperXml;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("ProductMapper.xml not found");
    }
}
