package com.bnk.paymentservice.exception;

import java.util.Collections;
import java.util.List;

/**
 * Client/input errors mapped to HTTP 400 by {@code ApiExceptionHandler}.
 */
public class BadRequestException extends RuntimeException {

    private final String code;
    private final List<String> details;

    public BadRequestException(String code, String message) {
        this(code, message, Collections.<String>emptyList(), null);
    }

    public BadRequestException(String code, String message, List<String> details) {
        this(code, message, details, null);
    }

    public BadRequestException(String code, String message, Throwable cause) {
        this(code, message, Collections.<String>emptyList(), cause);
    }

    public BadRequestException(String code, String message, List<String> details, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.details = details == null ? Collections.<String>emptyList() : details;
    }

    public String getCode() {
        return code;
    }

    public List<String> getDetails() {
        return details;
    }
}
