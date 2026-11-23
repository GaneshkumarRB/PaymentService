package com.bnk.paymentservice.service;

import com.bnk.paymentservice.api.model.AccountDetails;
import com.bnk.paymentservice.api.model.PaymentRequest;
import com.bnk.paymentservice.config.PaymentValidationProperties;
import com.bnk.paymentservice.exception.PaymentValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentValidatorTest {

    private PaymentValidator validator;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"), ZoneOffset.UTC);
        PaymentValidationProperties properties = new PaymentValidationProperties();
        properties.setSupportedCurrencies("USD,EUR,GBP");
        validator = new PaymentValidator();
        ReflectionTestUtils.setField(validator, "properties", properties);
        ReflectionTestUtils.setField(validator, "clock", clock);
    }

    @Test
    void acceptsValidRequest() {
        assertDoesNotThrow(() -> validator.validate(validRequest()));
    }

    @Test
    void rejectsNonPositiveAmount() {
        PaymentRequest request = validRequest();
        request.setAmount(0.0);
        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> validator.validate(request));
        assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("amount")));
    }

    @Test
    void rejectsUnsupportedCurrency() {
        PaymentRequest request = validRequest();
        request.setCurrency(null);
        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> validator.validate(request));
        assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("currency")));
    }

    @Test
    void rejectsInvalidAccountNumber() {
        PaymentRequest request = validRequest();
        request.getDestinationAccount().setAccountNumber("short");
        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> validator.validate(request));
        assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("accountNumber")));
    }

    @Test
    void rejectsPastExecutionDate() {
        PaymentRequest request = validRequest();
        request.setRequestedExecutionDate(LocalDate.of(2026, 11, 18));
        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> validator.validate(request));
        assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("requestedExecutionDate")));
    }

    private PaymentRequest validRequest() {
        PaymentRequest request = new PaymentRequest();
        AccountDetails source = new AccountDetails();
        source.setName("Alice");
        source.setAccountNumber("GB29NWBK60161331926819");
        AccountDetails destination = new AccountDetails();
        destination.setName("Bob");
        destination.setAccountNumber("DE89370400440532013000");
        request.setSourceAccount(source);
        request.setDestinationAccount(destination);
        request.setAmount(100.0);
        request.setCurrency(PaymentRequest.CurrencyEnum.EUR);
        request.setReference("INV-1");
        request.setRequestedExecutionDate(LocalDate.of(2026, 11, 19));
        return request;
    }
}
