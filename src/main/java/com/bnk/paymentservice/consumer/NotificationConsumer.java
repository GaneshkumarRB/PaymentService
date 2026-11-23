package com.bnk.paymentservice.consumer;

import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.messaging.EventHandler;
import com.bnk.paymentservice.models.PaymentSubmittedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Always-healthy notification side-effect stub.
 */
@Component
public class NotificationConsumer implements EventHandler {

    public static final String NAME = AppConstants.CONSUMER_NOTIFICATION;
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @Override
    public String consumerName() {
        return NAME;
    }

    @Override
    public void handle(PaymentSubmittedEvent event) {
        log.info("Notification sent for paymentId={} amount={} {} reference={}",
                event.paymentId(), event.amount(), event.currency(), event.reference());
    }
}
