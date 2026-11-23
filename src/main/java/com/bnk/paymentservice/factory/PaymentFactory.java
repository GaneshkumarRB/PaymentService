package com.bnk.paymentservice.factory;

import com.bnk.paymentservice.api.model.AccountDetails;
import com.bnk.paymentservice.api.model.FraudDecision;
import com.bnk.paymentservice.api.model.PaymentRequest;
import com.bnk.paymentservice.api.model.PaymentResponse;
import com.bnk.paymentservice.entity.AccountDetailsEmbeddable;
import com.bnk.paymentservice.entity.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Builds payment entities from requests and payment responses from persisted data.
 */
@Component
public class PaymentFactory {

    public Payment buildPayment(PaymentRequest request, UUID paymentId, Instant createdAt) {
        return new Payment(
                paymentId,
                buildAccountEntity(request.getSourceAccount()),
                buildAccountEntity(request.getDestinationAccount()),
                BigDecimal.valueOf(request.getAmount()).setScale(4, RoundingMode.HALF_UP),
                request.getCurrency().getValue(),
                request.getReference().trim(),
                request.getRequestedExecutionDate(),
                createdAt
        );
    }

    public PaymentResponse buildPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse()
                .id(payment.getId())
                .status(PaymentResponse.StatusEnum.fromValue(payment.getStatus().name()))
                .sourceAccount(buildAccountDetails(payment.getSourceAccount()))
                .destinationAccount(buildAccountDetails(payment.getDestinationAccount()))
                .amount(payment.getAmount().doubleValue())
                .currency(payment.getCurrency())
                .reference(payment.getReference())
                .requestedExecutionDate(payment.getRequestedExecutionDate())
                .createdAt(OffsetDateTime.ofInstant(payment.getCreatedAt(), ZoneOffset.UTC))
                .ledgerPosted(payment.isLedgerPosted());

        if (payment.getFraudDecision() != null) {
            FraudDecision decision = new FraudDecision()
                    .decision(FraudDecision.DecisionEnum.fromValue(payment.getFraudDecision().name()))
                    .riskScore(payment.getFraudRiskScore())
                    .reasons(payment.getFraudReasons());
            if (payment.getFraudScreenedAt() != null) {
                decision.screenedAt(OffsetDateTime.ofInstant(payment.getFraudScreenedAt(), ZoneOffset.UTC));
            }
            response.fraudDecision(decision);
        }
        return response;
    }

    private AccountDetailsEmbeddable buildAccountEntity(AccountDetails account) {
        return new AccountDetailsEmbeddable(account.getName(), account.getAccountNumber());
    }

    private AccountDetails buildAccountDetails(AccountDetailsEmbeddable account) {
        return new AccountDetails()
                .name(account.getName())
                .accountNumber(account.getAccountNumber());
    }
}