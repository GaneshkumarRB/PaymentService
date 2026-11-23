package com.bnk.paymentservice.exception;

public class FraudVendorException extends RuntimeException {

    private final String code;
    private final boolean retryable;

    public FraudVendorException(String code, String message, boolean retryable) {
        super(message);
        this.code = code;
        this.retryable = retryable;
    }

    public String getCode() {
        return code;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
