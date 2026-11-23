package com.bnk.paymentservice.exception;

import com.bnk.paymentservice.common.AppConstants;

import java.util.UUID;

public class PaymentNotFoundException extends BadRequestException {

    private final UUID paymentId;

    public PaymentNotFoundException(UUID paymentId) {
        super(AppConstants.CODE_NOT_FOUND, AppConstants.MSG_PAYMENT_NOT_FOUND_PREFIX + paymentId);
        this.paymentId = paymentId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }
}
