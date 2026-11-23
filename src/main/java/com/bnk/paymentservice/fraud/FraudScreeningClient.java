package com.bnk.paymentservice.fraud;

import com.bnk.paymentservice.models.FraudScreeningResult;
import com.bnk.paymentservice.models.PaymentSubmittedEvent;

public interface FraudScreeningClient {

    FraudScreeningResult screen(PaymentSubmittedEvent event);
}
