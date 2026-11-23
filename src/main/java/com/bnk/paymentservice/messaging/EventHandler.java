package com.bnk.paymentservice.messaging;

import com.bnk.paymentservice.models.PaymentSubmittedEvent;

/**
 * Defines a named consumer that handles submitted payment events.
 */
public interface EventHandler {

    String consumerName();

    void handle(PaymentSubmittedEvent event) throws Exception;
}
