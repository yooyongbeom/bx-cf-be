package com.bwg.channel.backend.businesscommon.validation;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.constants.enums.UseYn;
import com.bwg.channel.backend.businesscommon.domain.vo.BusinessDateRange;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 업무 입력값 공통 검증 유틸의 정상 변환과 실패 코드 검증
 */
class BusinessValidatorTests {

    @Test
    void acceptsExpectedAffectedRows() {
        assertThatCode(() -> BusinessValidator.requireAffectedRows(2, 2, "insertCodes"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsUnexpectedAffectedRowsWithOperationDetails() {
        assertThatThrownBy(() -> BusinessValidator.requireAffectedRows(0, 1, "insertGroup"))
                .isInstanceOfSatisfying(BwgBusinessException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.SERVER_ERROR);
                    assertThat(exception.getDetails())
                            .containsEntry("operation", "insertGroup")
                            .containsEntry("expectedRows", 1)
                            .containsEntry("actualRows", 0);
                });
    }

    @Test
    void returnsRequiredApiRequestData() {
        ApiRequest<String> request = new ApiRequest<>();
        request.setData("requestData");

        assertThat(BusinessValidator.requireData(request))
                .isEqualTo("requestData");
    }

    @Test
    void rejectsMissingApiRequestData() {
        ApiRequest<String> request = new ApiRequest<>();

        assertThatThrownBy(() -> BusinessValidator.requireData(request))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.REQUIRED_VALUE_MISSING);
    }

    @Test
    void rejectsMissingApiRequestWrapper() {
        assertThatThrownBy(() -> BusinessValidator.requireData(null))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.REQUIRED_VALUE_MISSING);
    }

    @Test
    void returnsTrimmedNonBlankValue() {
        assertThat(BusinessValidator.requireNonBlank(" product ", "productNm"))
                .isEqualTo("product");
    }

    @Test
    void rejectsBlankValueWithFieldDetail() {
        assertThatThrownBy(() -> BusinessValidator.requireNonBlank(" ", "productNm"))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.REQUIRED_VALUE_MISSING);
    }

    @Test
    void parsesRequiredUseYn() {
        assertThat(BusinessValidator.requireUseYn("Y", "useYn"))
                .isEqualTo(UseYn.Y);
    }

    @Test
    void createsRequiredDateRange() {
        BusinessDateRange range = BusinessValidator.requireDateRange(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                "searchPeriod"
        );

        assertThat(range.contains(LocalDate.of(2026, 1, 15))).isTrue();
    }
}
