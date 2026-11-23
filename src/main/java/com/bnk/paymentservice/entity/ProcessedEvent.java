package com.bnk.paymentservice.entity;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
@IdClass(ProcessedEvent.ProcessedEventId.class)
public class ProcessedEvent {

    @Id
    @Column(nullable = false, length = 64)
    private String consumerName;

    @Id
    @Type(type = "uuid-char")
    @Column(length = 36)
    private UUID eventId;

    @Column(nullable = false)
    private Instant processedAt;

    protected ProcessedEvent() {
    }

    public ProcessedEvent(String consumerName, UUID eventId, Instant processedAt) {
        this.consumerName = consumerName;
        this.eventId = eventId;
        this.processedAt = processedAt;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public static class ProcessedEventId implements Serializable {
        private String consumerName;
        private UUID eventId;

        public ProcessedEventId() {
        }

        public ProcessedEventId(String consumerName, UUID eventId) {
            this.consumerName = consumerName;
            this.eventId = eventId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ProcessedEventId)) {
                return false;
            }
            ProcessedEventId that = (ProcessedEventId) o;
            return Objects.equals(consumerName, that.consumerName) && Objects.equals(eventId, that.eventId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(consumerName, eventId);
        }
    }
}
