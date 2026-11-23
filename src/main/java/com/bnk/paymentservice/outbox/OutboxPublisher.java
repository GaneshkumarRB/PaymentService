package com.bnk.paymentservice.outbox;

import com.bnk.paymentservice.config.PaymentOutboxProperties;
import com.bnk.paymentservice.entity.OutboxMessage;
import com.bnk.paymentservice.messaging.InMemoryMessageBroker;
import com.bnk.paymentservice.models.OutboxStatus;
import com.bnk.paymentservice.models.PaymentSubmittedEvent;
import com.bnk.paymentservice.repository.OutboxMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

/**
 * Polls pending outbox rows and publishes them to the broker.
 * Survives process restarts and temporary broker unavailability because
 * unpublished rows remain PENDING until successfully handed to the broker.
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    @Autowired
    private OutboxMessageRepository outboxMessageRepository;
    @Autowired
    private InMemoryMessageBroker messageBroker;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PaymentOutboxProperties properties;
    @Autowired
    private Clock clock;

    @Scheduled(fixedDelayString = "${payment.outbox.poll-interval-ms:500}")
    @Transactional
    public void publishPending() {
        List<OutboxMessage> batch = outboxMessageRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING,
                PageRequest.of(0, properties.getBatchSize())
        );
        for (OutboxMessage message : batch) {
            try {
                PaymentSubmittedEvent event = objectMapper.readValue(message.getPayload(), PaymentSubmittedEvent.class);
                messageBroker.publish(event);
                message.markPublished(clock.instant());
                outboxMessageRepository.save(message);
            } catch (Exception ex) {
                log.warn("Failed to publish outbox message {}: {}", message.getId(), ex.getMessage());
            }
        }
    }
}
