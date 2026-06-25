package com.bwg.channel.backend.productsvc.cmm.exception;

import com.bwg.channel.backend.common.exception.BwgException;
import com.bwg.channel.backend.productsvc.cmm.constants.ProductErrorCode;

/**
 * product-svc 전용 비즈니스 예외
 */
public class BwgProductException extends BwgException {
    private static final long serialVersionUID = 1L;

    private BwgProductException(Builder builder) {
        super(builder);
    }

    /**
     * 상품 오류 코드 반환
     */
    @Override
    public ProductErrorCode getCode() {
        return (ProductErrorCode) super.getCode();
    }

    public static class Builder extends BwgException.Builder<Builder> {

        /**
         * 상품 오류 코드 설정
         */
        public Builder code(ProductErrorCode code) {
            super.code(code);
            return this;
        }

        /**
         * 상품 예외 인스턴스 생성
         */
        @Override
        public BwgProductException build() {
            return new BwgProductException(this);
        }
    }
}
