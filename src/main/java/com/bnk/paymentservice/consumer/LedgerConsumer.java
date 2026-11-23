package com.bnk.paymentservice.consumer;

import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.entity.LedgerEntry;
import com.bnk.paymentservice.messaging.EventHandler;
import com.bnk.paymentservice.models.PaymentSubmittedEvent;
import com.bnk.paymentservice.repository.LedgerEntryRepository;
import com.bnk.paymentservice.service.PaymentUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Posts a debit once per payment. Duplicate deliveries are ignored via unique paymentId.
 */
@Component
public class LedgerConsumer implements EventHandler {

    public static final String NAME = AppConstants.CONSUMER_LEDGER;
    private static final Logger log = LoggerFactory.getLogger(LedgerConsumer.class);

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;
    @Autowired
    private PaymentUpdateService paymentUpdateService;
    @Autowired
    private Clock clock;

    @Override
    public String consumerName() {
        return NAME;
    }

    @Override
    @Transactional
    public void handle(PaymentSubmittedEvent event) {
        if (!ledgerEntryRepository.existsByPaymentId(event.paymentId())) {
            try {
                ledgerEntryRepository.saveAndFlush(new LedgerEntry(
                        UUID.randomUUID(),
                        event.paymentId(),
                        event.sourceAccountNumber(),
                        event.amount(),
                        event.currency(),
                        clock.instant()
                ));
            } catch (DataIntegrityViolationException ex) {
                log.info("Ledger debit race lost for paymentId={}, treating as already posted", event.paymentId());
            }
        } else {
            log.info("Ledger debit already posted for paymentId={}, skipping insert", event.paymentId());
        }
        paymentUpdateService.markLedgerPostedWithRetry(event.paymentId());
        log.info("Ledger debit ensured for paymentId={} amount={} {}",
                event.paymentId(), event.amount(), event.currency());
    }
}
