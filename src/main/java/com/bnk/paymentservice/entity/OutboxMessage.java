package com.bnk.paymentservice.entity;

import com.bnk.paymentservice.models.OutboxStatus;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages")
public class OutboxMessage {

    @Id
    @Type(type = "uuid-char")
    @Column(length = 36)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String eventType;

    @Type(type = "uuid-char")
    @Column(nullable = false, length = 36)
    private UUID aggregateId;

    @Column(nullable = false, length = 8000)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OutboxStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant publishedAt;

    protected OutboxMessage() {
    }

    public OutboxMessage(UUID id, String eventType, UUID aggregateId, String payload, Instant createdAt) {
        this.id = id;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = createdAt;
    }

    public void markPublished(Instant publishedAt) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = publishedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}
