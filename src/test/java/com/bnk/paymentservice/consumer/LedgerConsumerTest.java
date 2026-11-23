package com.bnk.paymentservice.consumer;

import com.bnk.paymentservice.models.PaymentSubmittedEvent;
import com.bnk.paymentservice.repository.LedgerEntryRepository;
import com.bnk.paymentservice.service.PaymentUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerConsumerTest {

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;
    @Mock
    private PaymentUpdateService paymentUpdateService;

    @InjectMocks
    private LedgerConsumer consumer;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"), ZoneOffset.UTC);
        ReflectionTestUtils.setField(consumer, "clock", clock);
    }

    @Test
    void postsDebitOnce() {
        UUID paymentId = UUID.randomUUID();
        when(ledgerEntryRepository.existsByPaymentId(paymentId)).thenReturn(false);

        consumer.handle(event(paymentId));

        verify(ledgerEntryRepository).saveAndFlush(any());
        verify(paymentUpdateService).markLedgerPostedWithRetry(paymentId);
    }

    @Test
    void skipsDuplicatePaymentInsert() {
        UUID paymentId = UUID.randomUUID();
        when(ledgerEntryRepository.existsByPaymentId(paymentId)).thenReturn(true);

        consumer.handle(event(paymentId));

        verify(ledgerEntryRepository, never()).saveAndFlush(any());
        verify(paymentUpdateService).markLedgerPostedWithRetry(paymentId);
    }

    @Test
    void remainsIdempotentAcrossDuplicateCalls() {
        UUID paymentId = UUID.randomUUID();
        when(ledgerEntryRepository.existsByPaymentId(paymentId)).thenReturn(false, true);

        consumer.handle(event(paymentId));
        consumer.handle(event(paymentId));

        verify(ledgerEntryRepository, times(1)).saveAndFlush(any());
        verify(paymentUpdateService, times(2)).markLedgerPostedWithRetry(paymentId);
    }

    private PaymentSubmittedEvent event(UUID paymentId) {
        return new PaymentSubmittedEvent(
                UUID.randomUUID(),
                paymentId,
                "Alice",
                "GB29NWBK60161331926819",
                "Bob",
                "DE89370400440532013000",
                new BigDecimal("10.0000"),
                "EUR",
                "REF",
                LocalDate.of(2026, 11, 20),
                clock.instant()
        );
    }
}
