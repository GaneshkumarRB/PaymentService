package com.bnk.paymentservice.entity;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ledger_payment_id", columnNames = "paymentId")
})
public class LedgerEntry {

    @Id
    @Type(type = "uuid-char")
    @Column(length = 36)
    private UUID id;

    @Type(type = "uuid-char")
    @Column(nullable = false, unique = true, length = 36)
    private UUID paymentId;

    @Column(nullable = false, length = 34)
    private String sourceAccountNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private Instant postedAt;

    protected LedgerEntry() {
    }

    public LedgerEntry(UUID id,
                       UUID paymentId,
                       String sourceAccountNumber,
                       BigDecimal amount,
                       String currency,
                       Instant postedAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.sourceAccountNumber = sourceAccountNumber;
        this.amount = amount;
        this.currency = currency;
        this.postedAt = postedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getPostedAt() {
        return postedAt;
    }
}
