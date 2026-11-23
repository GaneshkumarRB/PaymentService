package com.bnk.paymentservice.fraud;

import com.bnk.paymentservice.models.FraudScreeningResult;
import com.bnk.paymentservice.models.PaymentSubmittedEvent;

/**
 * Defines fraud screening of a submitted payment event.
 */
public interface FraudScreeningClient {

    FraudScreeningResult screen(PaymentSubmittedEvent event);
}
