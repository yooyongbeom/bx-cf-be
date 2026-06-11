package com.bwg.channel.backend.productsvc.cmm.exception;

import com.bwg.channel.backend.common.exception.BwgException;
import com.bwg.channel.backend.productsvc.cmm.constants.ProductErrorCode;

public class BwgProductException extends BwgException {
    private static final long serialVersionUID = 1L;

    private BwgProductException(Builder builder) {
        super(builder);
    }

    @Override
    public ProductErrorCode getCode() {
        return (ProductErrorCode) super.getCode();
    }

    public static class Builder extends BwgException.Builder<Builder> {

        public Builder code(ProductErrorCode code) {
            super.code(code);
            return this;
        }

        @Override
        public BwgProductException build() {
            return new BwgProductException(this);
        }
    }
}
