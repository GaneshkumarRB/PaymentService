package com.bnk.paymentservice.consumer;

import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.exception.InternalServerException;
import com.bnk.paymentservice.entity.Payment;
import com.bnk.paymentservice.fraud.FraudScreeningClient;
import com.bnk.paymentservice.models.FraudScreeningResult;
import com.bnk.paymentservice.exception.FraudVendorException;
import com.bnk.paymentservice.messaging.EventHandler;
import com.bnk.paymentservice.models.PaymentSubmittedEvent;
import com.bnk.paymentservice.repository.PaymentRepository;
import com.bnk.paymentservice.service.PaymentUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Calls the third-party fraud screening provider and records the decision on the payment.
 * Retryable vendor failures propagate so the broker retries with backoff; non-retryable
 * failures still surface to the DLQ path after max attempts.
 */
@Component
public class FraudScreeningConsumer implements EventHandler {

    public static final String NAME = AppConstants.CONSUMER_FRAUD_SCREENING;
    private static final Logger log = LoggerFactory.getLogger(FraudScreeningConsumer.class);

    @Autowired
    private FraudScreeningClient fraudScreeningClient;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentUpdateService paymentUpdateService;
    @Autowired
    private Clock clock;

    @Override
    public String consumerName() {
        return NAME;
    }

    @Override
    public void handle(PaymentSubmittedEvent event) throws Exception {
        Payment existing = paymentRepository.findById(event.paymentId())
                .orElseThrow(() -> new InternalServerException(
                        AppConstants.CODE_PAYMENT_MISSING,
                        AppConstants.MSG_PAYMENT_MISSING_FOR_SCREENING_PREFIX + event.paymentId()
                ));
        if (existing.getFraudDecision() != null) {
            log.info("Fraud decision already present for paymentId={}, skipping", event.paymentId());
            return;
        }

        try {
            FraudScreeningResult result = fraudScreeningClient.screen(event);
            paymentUpdateService.applyFraudDecisionWithRetry(
                event.paymentId(),
                result.decision(),
                result.riskScore(),
                result.reasons(),
                clock.instant()
            );
            log.info("Fraud decision={} risk={} for paymentId={}",
                result.decision(), result.riskScore(), event.paymentId());
        } catch (FraudVendorException ex) {
            if (!ex.isRetryable()) {
                log.error("Non-retryable fraud vendor error for paymentId={}: {}", event.paymentId(), ex.getMessage());
            }
            throw ex;
        }
        
    }
}
