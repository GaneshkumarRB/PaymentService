package com.bnk.paymentservice.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class PaymentSubmittedEvent {

    private final UUID eventId;
    private final UUID paymentId;
    private final String sourceAccountName;
    private final String sourceAccountNumber;
    private final String destinationAccountName;
    private final String destinationAccountNumber;
    private final BigDecimal amount;
    private final String currency;
    private final String reference;
    private final LocalDate requestedExecutionDate;
    private final Instant occurredAt;

    @JsonCreator
    public PaymentSubmittedEvent(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("paymentId") UUID paymentId,
            @JsonProperty("sourceAccountName") String sourceAccountName,
            @JsonProperty("sourceAccountNumber") String sourceAccountNumber,
            @JsonProperty("destinationAccountName") String destinationAccountName,
            @JsonProperty("destinationAccountNumber") String destinationAccountNumber,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("currency") String currency,
            @JsonProperty("reference") String reference,
            @JsonProperty("requestedExecutionDate") LocalDate requestedExecutionDate,
            @JsonProperty("occurredAt") Instant occurredAt) {
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.sourceAccountName = sourceAccountName;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountName = destinationAccountName;
        this.destinationAccountNumber = destinationAccountNumber;
        this.amount = amount;
        this.currency = currency;
        this.reference = reference;
        this.requestedExecutionDate = requestedExecutionDate;
        this.occurredAt = occurredAt;
    }

    @JsonProperty("eventId")
    public UUID eventId() {
        return eventId;
    }

    @JsonProperty("paymentId")
    public UUID paymentId() {
        return paymentId;
    }

    @JsonProperty("sourceAccountName")
    public String sourceAccountName() {
        return sourceAccountName;
    }

    @JsonProperty("sourceAccountNumber")
    public String sourceAccountNumber() {
        return sourceAccountNumber;
    }

    @JsonProperty("destinationAccountName")
    public String destinationAccountName() {
        return destinationAccountName;
    }

    @JsonProperty("destinationAccountNumber")
    public String destinationAccountNumber() {
        return destinationAccountNumber;
    }

    @JsonProperty("amount")
    public BigDecimal amount() {
        return amount;
    }

    @JsonProperty("currency")
    public String currency() {
        return currency;
    }

    @JsonProperty("reference")
    public String reference() {
        return reference;
    }

    @JsonProperty("requestedExecutionDate")
    public LocalDate requestedExecutionDate() {
        return requestedExecutionDate;
    }

    @JsonProperty("occurredAt")
    public Instant occurredAt() {
        return occurredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentSubmittedEvent)) {
            return false;
        }
        PaymentSubmittedEvent that = (PaymentSubmittedEvent) o;
        return Objects.equals(eventId, that.eventId)
                && Objects.equals(paymentId, that.paymentId)
                && Objects.equals(sourceAccountName, that.sourceAccountName)
                && Objects.equals(sourceAccountNumber, that.sourceAccountNumber)
                && Objects.equals(destinationAccountName, that.destinationAccountName)
                && Objects.equals(destinationAccountNumber, that.destinationAccountNumber)
                && Objects.equals(amount, that.amount)
                && Objects.equals(currency, that.currency)
                && Objects.equals(reference, that.reference)
                && Objects.equals(requestedExecutionDate, that.requestedExecutionDate)
                && Objects.equals(occurredAt, that.occurredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, paymentId, sourceAccountName, sourceAccountNumber,
                destinationAccountName, destinationAccountNumber, amount, currency, reference,
                requestedExecutionDate, occurredAt);
    }

    @Override
    public String toString() {
        return "PaymentSubmittedEvent{"
                + "eventId=" + eventId
                + ", paymentId=" + paymentId
                + ", sourceAccountName='" + sourceAccountName + '\''
                + ", sourceAccountNumber='" + sourceAccountNumber + '\''
                + ", destinationAccountName='" + destinationAccountName + '\''
                + ", destinationAccountNumber='" + destinationAccountNumber + '\''
                + ", amount=" + amount
                + ", currency='" + currency + '\''
                + ", reference='" + reference + '\''
                + ", requestedExecutionDate=" + requestedExecutionDate
                + ", occurredAt=" + occurredAt
                + '}';
    }
}
