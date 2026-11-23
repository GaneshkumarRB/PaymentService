package com.bnk.paymentservice.exception;

import com.bnk.paymentservice.common.AppConstants;

import java.util.List;

/**
 * Reports validation failures for a payment request.
 */
public class PaymentValidationException extends BadRequestException {

    public PaymentValidationException(String code, String message, List<String> details) {
        super(code, message, details);
    }

    public static PaymentValidationException invalid(List<String> details) {
        return new PaymentValidationException(
                AppConstants.CODE_VALIDATION_ERROR,
                AppConstants.MSG_PAYMENT_REQUEST_INVALID,
                details
        );
    }
}
