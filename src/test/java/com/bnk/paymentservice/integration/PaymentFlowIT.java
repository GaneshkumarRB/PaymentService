package com.bnk.paymentservice.integration;

import com.bnk.paymentservice.api.model.AccountDetails;
import com.bnk.paymentservice.api.model.PaymentRequest;
import com.bnk.paymentservice.api.model.PaymentResponse;
import com.bnk.paymentservice.config.FraudClientProperties;
import com.bnk.paymentservice.models.OutboxStatus;
import com.bnk.paymentservice.repository.DeadLetterMessageRepository;
import com.bnk.paymentservice.repository.LedgerEntryRepository;
import com.bnk.paymentservice.repository.OutboxMessageRepository;
import com.bnk.paymentservice.repository.PaymentRepository;
import com.bnk.paymentservice.repository.ProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentFlowIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FraudClientProperties fraudClientProperties;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private DeadLetterMessageRepository deadLetterMessageRepository;

    @BeforeEach
    void pointFraudClientAtLocalStub() {
        fraudClientProperties.setBaseUrl("http://localhost:" + port);
    }

    @Test
    void submitsPaymentPublishesOutboxAndConsumersProcess() {
        PaymentRequest request = new PaymentRequest();
        AccountDetails source = new AccountDetails();
        source.setName("Alice");
        source.setAccountNumber("GB29NWBK60161331926819");
        AccountDetails destination = new AccountDetails();
        destination.setName("Bob");
        destination.setAccountNumber("DE89370400440532013000");
        request.setSourceAccount(source);
        request.setDestinationAccount(destination);
        request.setAmount(250.75);
        request.setCurrency(PaymentRequest.CurrencyEnum.EUR);
        request.setReference("INV-100");
        request.setRequestedExecutionDate(LocalDate.now().plusDays(1));

        ResponseEntity<PaymentResponse> created = restTemplate.postForEntity("/payments", request, PaymentResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertNotNull(created.getBody());
        UUID paymentId = created.getBody().getId();

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(outboxMessageRepository.findAll().stream()
                    .anyMatch(m -> m.getAggregateId().equals(paymentId) && m.getStatus() == OutboxStatus.PUBLISHED));
            assertTrue(ledgerEntryRepository.existsByPaymentId(paymentId));
            assertTrue(paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new AssertionError("payment missing"))
                    .getFraudDecision() != null);
            assertTrue(processedEventRepository.count() >= 3);
        });

        ResponseEntity<PaymentResponse> fetched = restTemplate.getForEntity("/payments/" + paymentId, PaymentResponse.class);
        assertEquals(HttpStatus.OK, fetched.getStatusCode());
        assertNotNull(fetched.getBody());
        assertNotNull(fetched.getBody().getFraudDecision());
        assertTrue(fetched.getBody().getLedgerPosted());
        assertEquals(0, deadLetterMessageRepository.count());
    }

    @Test
    void declinesPaymentAboveRiskThreshold() {
        PaymentRequest request = new PaymentRequest();
        AccountDetails source = new AccountDetails();
        source.setName("Alice");
        source.setAccountNumber("GB29NWBK60161331926819");
        AccountDetails destination = new AccountDetails();
        destination.setName("Bob");
        destination.setAccountNumber("DE89370400440532013000");
        request.setSourceAccount(source);
        request.setDestinationAccount(destination);
        request.setAmount(1200.0);
        request.setCurrency(PaymentRequest.CurrencyEnum.EUR);
        request.setReference("demo-1");
        request.setRequestedExecutionDate(LocalDate.now().plusDays(1));

        ResponseEntity<PaymentResponse> created = restTemplate.postForEntity("/payments", request, PaymentResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertNotNull(created.getBody());
        UUID paymentId = created.getBody().getId();

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            ResponseEntity<PaymentResponse> fetched = restTemplate.getForEntity("/payments/" + paymentId, PaymentResponse.class);
            assertEquals(HttpStatus.OK, fetched.getStatusCode());
            assertNotNull(fetched.getBody());
            assertNotNull(fetched.getBody().getFraudDecision());
            assertEquals("DECLINE", fetched.getBody().getFraudDecision().getDecision().toString());
            assertEquals("FAILED", fetched.getBody().getStatus().toString());
        });
    }

    @Test
    void rejectsInvalidPayment() {
        PaymentRequest request = new PaymentRequest();
        AccountDetails source = new AccountDetails();
        source.setName("Alice");
        source.setAccountNumber("bad");
        AccountDetails destination = new AccountDetails();
        destination.setName("Bob");
        destination.setAccountNumber("DE89370400440532013000");
        request.setSourceAccount(source);
        request.setDestinationAccount(destination);
        request.setAmount(-1.0);
        request.setCurrency(PaymentRequest.CurrencyEnum.USD);
        request.setReference("x");
        request.setRequestedExecutionDate(LocalDate.now().minusDays(2));

        ResponseEntity<String> response = restTemplate.postForEntity("/payments", request, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void returnsBadRequestForUnknownPayment() {
        ResponseEntity<String> response = restTemplate.getForEntity("/payments/" + UUID.randomUUID(), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
