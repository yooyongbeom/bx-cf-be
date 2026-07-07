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
