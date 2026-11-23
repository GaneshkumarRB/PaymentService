package com.bnk.paymentservice.messaging;

import com.bnk.paymentservice.models.PaymentSubmittedEvent;

public interface EventHandler {

    String consumerName();

    void handle(PaymentSubmittedEvent event) throws Exception;
}
