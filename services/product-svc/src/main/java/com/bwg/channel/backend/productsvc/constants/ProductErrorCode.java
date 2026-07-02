package com.bwg.channel.backend.productsvc.constants;

import com.bwg.channel.backend.common.constants.enums.BwgErrorCode;

/**
 * product-svc 비즈니스 오류 코드
 */
public enum ProductErrorCode implements BwgErrorCode {

    // 입력값 검증 (-5001 ~ -5099)
    REQUIRED_VALUE_MISSING("-5001", "필수값이 없습니다"),
    INVALID_PRODUCT_ID    ("-5002", "유효하지 않은 상품 ID입니다"),
    INVALID_PRICE         ("-5003", "가격은 0 이상이어야 합니다"),
    INVALID_STOCK_QTY     ("-5004", "재고 수량은 0 이상이어야 합니다"),

    // 조회 (-5101 ~ -5199)
    PRODUCT_NOT_FOUND  ("-5101", "상품을 찾을 수 없습니다"),
    PRODUCT_LIST_EMPTY ("-5102", "조회된 상품이 없습니다"),

    // 저장/수정/삭제 (-5201 ~ -5299)
    PRODUCT_SAVE_ERROR  ("-5201", "상품 저장에 실패했습니다"),
    PRODUCT_UPDATE_ERROR("-5202", "상품 수정에 실패했습니다"),
    PRODUCT_DELETE_ERROR("-5203", "상품 삭제에 실패했습니다"),

    // 비즈니스 규칙 (-5301 ~ -5399)
    PRODUCT_INACTIVE("-5301", "비활성화된 상품입니다"),
    OUT_OF_STOCK    ("-5302", "재고가 부족합니다"),

    // 서버 오류 (-5999)
    SERVER_ERROR("-5999", "상품 서비스 내부 오류");

    private final String code;
    private final String msg;

    ProductErrorCode(String code, String msg) {
        this.code = code;
        this.msg  = msg;
    }

    @Override public String getCode() { return code; }
    @Override public String getMsg()  { return msg;  }

    /**
     * 문자열 코드 기준 ProductErrorCode 변환
     */
    public static ProductErrorCode fromCode(String code) {
        for (ProductErrorCode e : values()) {
            if (e.code.equals(code)) return e;
        }
        throw new IllegalArgumentException("Unknown ProductErrorCode: " + code);
    }
}
