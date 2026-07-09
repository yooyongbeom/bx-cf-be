package com.bwg.channel.backend.productsvc.constants;

import com.bwg.channel.backend.common.constants.error.BwgErrorCode;
import org.springframework.http.HttpStatus;

/**
 * product-svc 비즈니스 에러 코드.
 */
public enum ProductErrorCode implements BwgErrorCode {

    // 입력값 검증 (-5001 ~ -5099)
    REQUIRED_VALUE_MISSING("-5001", "필수값이 없습니다", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_ID("-5002", "유효하지 않은 상품 ID입니다", HttpStatus.BAD_REQUEST),
    INVALID_PRICE("-5003", "가격은 0 이상이어야 합니다", HttpStatus.BAD_REQUEST),
    INVALID_STOCK_QTY("-5004", "재고 수량은 0 이상이어야 합니다", HttpStatus.BAD_REQUEST),

    // 조회 (-5101 ~ -5199)
    PRODUCT_NOT_FOUND("-5101", "상품을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    PRODUCT_LIST_EMPTY("-5102", "조회된 상품이 없습니다", HttpStatus.NOT_FOUND),

    // 저장/수정/삭제 (-5201 ~ -5299)
    PRODUCT_SAVE_ERROR("-5201", "상품 저장에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    PRODUCT_UPDATE_ERROR("-5202", "상품 수정에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    PRODUCT_DELETE_ERROR("-5203", "상품 삭제에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),

    // 비즈니스 규칙 (-5301 ~ -5399)
    PRODUCT_INACTIVE("-5301", "비활성화된 상품입니다", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK("-5302", "재고가 부족합니다", HttpStatus.BAD_REQUEST),

    // 서버 오류 (-5999)
    SERVER_ERROR("-5999", "상품 서비스 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String msg;
    private final HttpStatus status;

    ProductErrorCode(String code, String msg, HttpStatus status) {
        this.code = code;
        this.msg = msg;
        this.status = status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    public static ProductErrorCode fromCode(String code) {
        for (ProductErrorCode e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown ProductErrorCode: " + code);
    }
}
