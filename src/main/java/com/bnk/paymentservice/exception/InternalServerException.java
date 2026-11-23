package com.bnk.paymentservice.exception;

/**
 * Unexpected/system errors mapped to HTTP 500 by {@code ApiExceptionHandler}.
 */
public class InternalServerException extends RuntimeException {

    private final String code;

    public InternalServerException(String code, String message) {
        super(message);
        this.code = code;
    }

    public InternalServerException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
