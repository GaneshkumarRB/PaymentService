package com.bnk.paymentservice.factory;

import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.exception.FraudVendorException;
import com.bnk.paymentservice.fraud.api.model.Error;
import com.bnk.paymentservice.fraud.api.model.Reason;
import com.bnk.paymentservice.fraud.api.model.ScreeningRequest;
import com.bnk.paymentservice.fraud.api.model.ScreeningResponse;
import com.bnk.paymentservice.fraud.api.model.ScreeningResult;
import com.bnk.paymentservice.models.FraudDecisionType;
import com.bnk.paymentservice.models.FraudScreeningResult;
import com.bnk.paymentservice.models.PaymentSubmittedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FraudScreeningFactoryTest {

    private final FraudScreeningFactory factory = new FraudScreeningFactory();

    @Test
    void buildsScreeningRequestFromPaymentEvent() {
        UUID paymentId = UUID.randomUUID();
        PaymentSubmittedEvent event = new PaymentSubmittedEvent(
                UUID.randomUUID(), paymentId, "Alice", "GB29NWBK60161331926819",
                "Bob", "DE89370400440532013000", new BigDecimal("1200.00"), "EUR", "demo-1",
                LocalDate.of(2030, 1, 15), Instant.parse("2026-11-23T06:57:38.971Z"));

        ScreeningRequest request = factory.buildScreeningRequest(event);

        assertEquals(paymentId, request.getPaymentId());
        assertEquals(1200.0, request.getAmount());
        assertEquals("EUR", request.getCurrency());
        assertEquals("GB29NWBK60161331926819", request.getSourceAccountNumber());
        assertEquals("DE89370400440532013000", request.getDestinationAccountNumber());
        assertEquals("demo-1", request.getReference());
    }

    @Test
    void buildsDomainResultFromScreeningResponse() {
        ScreeningResult result = new ScreeningResult()
                .decision(ScreeningResult.DecisionEnum.DECLINE)
                .riskScore(95)
                .reasons(Arrays.asList(new Reason().value("New payee"), null,
                        new Reason(), new Reason().value("High amount")));

        FraudScreeningResult domain = factory.buildScreeningResult(new ScreeningResponse().result(result));

        assertEquals(FraudDecisionType.DECLINE, domain.decision());
        assertEquals(95, domain.riskScore());
        assertEquals(Arrays.asList("New payee", "High amount"), domain.reasons());
    }

    @Test
    void mapsMissingReasonsToEmptyList() {
        ScreeningResult result = new ScreeningResult().decision(ScreeningResult.DecisionEnum.ACCEPT).riskScore(12);
        assertEquals(Collections.emptyList(), factory.buildScreeningResult(new ScreeningResponse().result(result)).reasons());
    }

    @Test
    void rejectsEmptyAndMalformedResponses() {
        FraudVendorException empty = assertThrows(FraudVendorException.class,
                () -> factory.buildScreeningResult(null));
        assertEquals(AppConstants.CODE_EMPTY_RESPONSE, empty.getCode());
        assertTrue(empty.isRetryable());

        for (ScreeningResponse response : Arrays.asList(
                new ScreeningResponse(),
                new ScreeningResponse().result(new ScreeningResult().riskScore(10)),
                new ScreeningResponse().result(new ScreeningResult().decision(ScreeningResult.DecisionEnum.ACCEPT)))) {
            FraudVendorException malformed = assertThrows(FraudVendorException.class,
                    () -> factory.buildScreeningResult(response));
            assertEquals(AppConstants.CODE_MALFORMED_RESPONSE, malformed.getCode());
            assertTrue(malformed.isRetryable());
        }
    }

    @Test
    void mapsVendorErrorsAndRetryDefaults() {
        FraudVendorException error = assertThrows(FraudVendorException.class,
                () -> factory.buildScreeningResult(new ScreeningResponse().error(
                        new Error().code("FRAUD-1").message("Rejected").retryable(true))));
        assertEquals("FRAUD-1", error.getCode());
        assertEquals("Rejected", error.getMessage());
        assertTrue(error.isRetryable());

        FraudVendorException defaults = assertThrows(FraudVendorException.class,
                () -> factory.buildScreeningResult(new ScreeningResponse().error(new Error())));
        assertEquals(AppConstants.CODE_VENDOR_ERROR, defaults.getCode());
        assertEquals(AppConstants.MSG_VENDOR_SCREENING_FAILED, defaults.getMessage());
        assertFalse(defaults.isRetryable());
        assertTrue(factory.buildVendorException(new Error(), true).isRetryable());
        assertFalse(factory.buildVendorException(new Error().retryable(false), true).isRetryable());
    }
}