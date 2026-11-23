package com.bnk.paymentservice.factory;

import com.bnk.paymentservice.api.model.AccountDetails;
import com.bnk.paymentservice.api.model.FraudDecision;
import com.bnk.paymentservice.api.model.PaymentRequest;
import com.bnk.paymentservice.api.model.PaymentResponse;
import com.bnk.paymentservice.entity.Payment;
import com.bnk.paymentservice.models.FraudDecisionType;
import com.bnk.paymentservice.models.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentFactoryTest {

    private final PaymentFactory factory = new PaymentFactory();
    private final UUID paymentId = UUID.randomUUID();
    private final Instant createdAt = Instant.parse("2026-11-23T06:57:38.971Z");
    private final LocalDate executionDate = LocalDate.of(2030, 1, 15);

    @Test
    void buildsPaymentFromRequest() {
        Payment payment = buildPayment();

        assertEquals(paymentId, payment.getId());
        assertEquals(PaymentStatus.ACCEPTED, payment.getStatus());
        assertEquals("Alice", payment.getSourceAccount().getName());
        assertEquals("GB29NWBK60161331926819", payment.getSourceAccount().getAccountNumber());
        assertEquals("Bob", payment.getDestinationAccount().getName());
        assertEquals("DE89370400440532013000", payment.getDestinationAccount().getAccountNumber());
        assertEquals(new BigDecimal("1200.1235"), payment.getAmount());
        assertEquals("EUR", payment.getCurrency());
        assertEquals("demo-1", payment.getReference());
        assertEquals(executionDate, payment.getRequestedExecutionDate());
        assertEquals(createdAt, payment.getCreatedAt());
        assertFalse(payment.isLedgerPosted());
    }

    @Test
    void buildsResponseWithoutFraudDecision() {
        PaymentResponse response = factory.buildPaymentResponse(buildPayment());

        assertEquals(paymentId, response.getId());
        assertEquals(PaymentResponse.StatusEnum.ACCEPTED, response.getStatus());
        assertEquals("Alice", response.getSourceAccount().getName());
        assertEquals("GB29NWBK60161331926819", response.getSourceAccount().getAccountNumber());
        assertEquals("Bob", response.getDestinationAccount().getName());
        assertEquals("DE89370400440532013000", response.getDestinationAccount().getAccountNumber());
        assertEquals(1200.1235, response.getAmount());
        assertEquals("EUR", response.getCurrency());
        assertEquals("demo-1", response.getReference());
        assertEquals(executionDate, response.getRequestedExecutionDate());
        assertEquals(OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC), response.getCreatedAt());
        assertFalse(response.getLedgerPosted());
        assertNull(response.getFraudDecision());
    }

    @Test
    void buildsResponseWithFraudDecision() {
        Payment payment = buildPayment();
        Instant screenedAt = Instant.parse("2026-11-23T06:57:40.281Z");
        payment.applyFraudDecision(FraudDecisionType.DECLINE, 95, Arrays.asList("High amount", "New payee"), screenedAt);
        payment.markLedgerPosted();

        PaymentResponse response = factory.buildPaymentResponse(payment);

        assertEquals(PaymentResponse.StatusEnum.FAILED, response.getStatus());
        assertTrue(response.getLedgerPosted());
        assertNotNull(response.getFraudDecision());
        assertEquals(FraudDecision.DecisionEnum.DECLINE, response.getFraudDecision().getDecision());
        assertEquals(95, response.getFraudDecision().getRiskScore());
        assertEquals(Arrays.asList("High amount", "New payee"), response.getFraudDecision().getReasons());
        assertEquals(OffsetDateTime.ofInstant(screenedAt, ZoneOffset.UTC), response.getFraudDecision().getScreenedAt());
    }

    @Test
    void buildsFraudDecisionWithoutScreenedAt() {
        Payment payment = buildPayment();
        payment.applyFraudDecision(FraudDecisionType.ACCEPT, 12, null, null);

        FraudDecision decision = factory.buildPaymentResponse(payment).getFraudDecision();

        assertNotNull(decision);
        assertEquals(FraudDecision.DecisionEnum.ACCEPT, decision.getDecision());
        assertTrue(decision.getReasons().isEmpty());
        assertNull(decision.getScreenedAt());
    }

    private Payment buildPayment() {
        PaymentRequest request = new PaymentRequest()
                .sourceAccount(new AccountDetails().name("Alice").accountNumber("GB29NWBK60161331926819"))
                .destinationAccount(new AccountDetails().name("Bob").accountNumber("DE89370400440532013000"))
                .amount(1200.12346)
                .currency(PaymentRequest.CurrencyEnum.EUR)
                .reference("  demo-1  ")
                .requestedExecutionDate(executionDate);
        return factory.buildPayment(request, paymentId, createdAt);
    }
}