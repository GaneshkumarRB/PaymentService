package com.bnk.paymentservice.entity;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dead_letter_messages")
public class DeadLetterMessage {

    @Id
    @Type(type = "uuid-char")
    @Column(length = 36)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String consumerName;

    @Type(type = "uuid-char")
    @Column(nullable = false, length = 36)
    private UUID eventId;

    @Type(type = "uuid-char")
    @Column(nullable = false, length = 36)
    private UUID paymentId;

    @Column(nullable = false, length = 8000)
    private String payload;

    @Column(nullable = false, length = 1000)
    private String lastError;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private Instant failedAt;

    protected DeadLetterMessage() {
    }

    public DeadLetterMessage(UUID id,
                             String consumerName,
                             UUID eventId,
                             UUID paymentId,
                             String payload,
                             String lastError,
                             int attempts,
                             Instant failedAt) {
        this.id = id;
        this.consumerName = consumerName;
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.payload = payload;
        this.lastError = lastError;
        this.attempts = attempts;
        this.failedAt = failedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public String getPayload() {
        return payload;
    }

    public String getLastError() {
        return lastError;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getFailedAt() {
        return failedAt;
    }
}
