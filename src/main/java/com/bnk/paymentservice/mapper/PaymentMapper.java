package com.bnk.paymentservice.mapper;

import com.bnk.paymentservice.api.model.AccountDetails;
import com.bnk.paymentservice.api.model.FraudDecision;
import com.bnk.paymentservice.api.model.PaymentResponse;
import com.bnk.paymentservice.entity.AccountDetailsEmbeddable;
import com.bnk.paymentservice.entity.Payment;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setStatus(PaymentResponse.StatusEnum.fromValue(payment.getStatus().name()));
        response.setSourceAccount(toAccount(payment.getSourceAccount()));
        response.setDestinationAccount(toAccount(payment.getDestinationAccount()));
        response.setAmount(payment.getAmount().doubleValue());
        response.setCurrency(payment.getCurrency());
        response.setReference(payment.getReference());
        response.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        response.setCreatedAt(OffsetDateTime.ofInstant(payment.getCreatedAt(), ZoneOffset.UTC));
        response.setLedgerPosted(payment.isLedgerPosted());
        if (payment.getFraudDecision() != null) {
            FraudDecision decision = new FraudDecision();
            decision.setDecision(FraudDecision.DecisionEnum.fromValue(payment.getFraudDecision().name()));
            decision.setRiskScore(payment.getFraudRiskScore());
            decision.setReasons(payment.getFraudReasons());
            if (payment.getFraudScreenedAt() != null) {
                decision.setScreenedAt(OffsetDateTime.ofInstant(payment.getFraudScreenedAt(), ZoneOffset.UTC));
            }
            response.setFraudDecision(decision);
        }
        return response;
    }

    private AccountDetails toAccount(AccountDetailsEmbeddable embeddable) {
        AccountDetails details = new AccountDetails();
        details.setName(embeddable.getName());
        details.setAccountNumber(embeddable.getAccountNumber());
        return details;
    }
}
