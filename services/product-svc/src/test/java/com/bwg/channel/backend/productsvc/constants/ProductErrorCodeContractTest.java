package com.bwg.channel.backend.productsvc.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ProductErrorCodeContractTest {

    @Test
    void productErrorCodesUseProductRangeAndStatus() {
        assertThat(ProductErrorCode.PRODUCT_NOT_FOUND.getCode()).startsWith("-5");
        assertThat(ProductErrorCode.PRODUCT_NOT_FOUND.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ProductErrorCode.PRODUCT_INACTIVE.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void productErrorCodesDoNotContainDuplicateCodes() {
        long uniqueCount = Arrays.stream(ProductErrorCode.values())
                .map(ProductErrorCode::getCode)
                .distinct()
                .count();

        assertThat(uniqueCount).isEqualTo(ProductErrorCode.values().length);
    }
}
