package com.bnk.paymentservice.models;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class BrokerMessage {

    private final UUID deliveryId;
    private final String consumerName;
    private final PaymentSubmittedEvent event;
    private final int attempt;
    private final Instant availableAt;

    public BrokerMessage(UUID deliveryId,
                         String consumerName,
                         PaymentSubmittedEvent event,
                         int attempt,
                         Instant availableAt) {
        this.deliveryId = deliveryId;
        this.consumerName = consumerName;
        this.event = event;
        this.attempt = attempt;
        this.availableAt = availableAt;
    }

    public BrokerMessage nextAttempt(Instant nextAvailableAt) {
        return new BrokerMessage(deliveryId, consumerName, event, attempt + 1, nextAvailableAt);
    }

    public UUID deliveryId() {
        return deliveryId;
    }

    public String consumerName() {
        return consumerName;
    }

    public PaymentSubmittedEvent event() {
        return event;
    }

    public int attempt() {
        return attempt;
    }

    public Instant availableAt() {
        return availableAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BrokerMessage)) {
            return false;
        }
        BrokerMessage that = (BrokerMessage) o;
        return attempt == that.attempt
                && Objects.equals(deliveryId, that.deliveryId)
                && Objects.equals(consumerName, that.consumerName)
                && Objects.equals(event, that.event)
                && Objects.equals(availableAt, that.availableAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryId, consumerName, event, attempt, availableAt);
    }

    @Override
    public String toString() {
        return "BrokerMessage{"
                + "deliveryId=" + deliveryId
                + ", consumerName='" + consumerName + '\''
                + ", event=" + event
                + ", attempt=" + attempt
                + ", availableAt=" + availableAt
                + '}';
    }
}
