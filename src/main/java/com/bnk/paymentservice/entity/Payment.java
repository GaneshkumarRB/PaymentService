package com.bnk.paymentservice.entity;

import com.bnk.paymentservice.models.FraudDecisionType;
import com.bnk.paymentservice.models.PaymentStatus;
import org.hibernate.annotations.Type;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @Type(type = "uuid-char")
    @Column(length = 36)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "source_name", nullable = false, length = 120)),
            @AttributeOverride(name = "accountNumber", column = @Column(name = "source_account_number", nullable = false, length = 34))
    })
    private AccountDetailsEmbeddable sourceAccount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "destination_name", nullable = false, length = 120)),
            @AttributeOverride(name = "accountNumber", column = @Column(name = "destination_account_number", nullable = false, length = 34))
    })
    private AccountDetailsEmbeddable destinationAccount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 140)
    private String reference;

    @Column(nullable = false)
    private LocalDate requestedExecutionDate;

    @Column(nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private FraudDecisionType fraudDecision;

    private Integer fraudRiskScore;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "payment_fraud_reasons", joinColumns = @JoinColumn(name = "payment_id"))
    @Column(name = "reason")
    private List<String> fraudReasons = new ArrayList<>();

    private Instant fraudScreenedAt;

    @Column(nullable = false)
    private boolean ledgerPosted;

    @Version
    private long version;

    protected Payment() {
    }

    public Payment(UUID id,
                   AccountDetailsEmbeddable sourceAccount,
                   AccountDetailsEmbeddable destinationAccount,
                   BigDecimal amount,
                   String currency,
                   String reference,
                   LocalDate requestedExecutionDate,
                   Instant createdAt) {
        this.id = id;
        this.status = PaymentStatus.ACCEPTED;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.currency = currency;
        this.reference = reference;
        this.requestedExecutionDate = requestedExecutionDate;
        this.createdAt = createdAt;
        this.ledgerPosted = false;
    }

    public void markScreening() {
        this.status = PaymentStatus.SCREENING;
    }

    public void applyFraudDecision(FraudDecisionType decision, Integer riskScore, List<String> reasons, Instant screenedAt) {
        this.fraudDecision = decision;
        this.fraudRiskScore = riskScore;
        this.fraudReasons = reasons == null ? new ArrayList<String>() : new ArrayList<>(reasons);
        this.fraudScreenedAt = screenedAt;
        if (decision == FraudDecisionType.DECLINE) {
            this.status = PaymentStatus.FAILED;
        } else if (this.ledgerPosted) {
            this.status = PaymentStatus.COMPLETED;
        } else {
            this.status = PaymentStatus.SCREENING;
        }
    }

    public void markLedgerPosted() {
        this.ledgerPosted = true;
        if (this.fraudDecision != null && this.fraudDecision != FraudDecisionType.DECLINE) {
            this.status = PaymentStatus.COMPLETED;
        }
    }

    public UUID getId() {
        return id;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public AccountDetailsEmbeddable getSourceAccount() {
        return sourceAccount;
    }

    public AccountDetailsEmbeddable getDestinationAccount() {
        return destinationAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getReference() {
        return reference;
    }

    public LocalDate getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public FraudDecisionType getFraudDecision() {
        return fraudDecision;
    }

    public Integer getFraudRiskScore() {
        return fraudRiskScore;
    }

    public List<String> getFraudReasons() {
        return fraudReasons;
    }

    public Instant getFraudScreenedAt() {
        return fraudScreenedAt;
    }

    public boolean isLedgerPosted() {
        return ledgerPosted;
    }
}
