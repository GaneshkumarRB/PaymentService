package com.bnk.paymentservice.integration;

import com.bnk.paymentservice.api.model.AccountDetails;
import com.bnk.paymentservice.api.model.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentValidationIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void rejectsPastExecutionDateWith400() {
        PaymentRequest request = new PaymentRequest();
        AccountDetails source = new AccountDetails();
        source.setName("Alice");
        source.setAccountNumber("GB29NWBK60161331926819");
        AccountDetails destination = new AccountDetails();
        destination.setName("Bob");
        destination.setAccountNumber("DE89370400440532013000");
        request.setSourceAccount(source);
        request.setDestinationAccount(destination);
        request.setAmount(10.0);
        request.setCurrency(PaymentRequest.CurrencyEnum.EUR);
        request.setReference("past-date");
        request.setRequestedExecutionDate(LocalDate.now(ZoneOffset.UTC).minusDays(1));

        ResponseEntity<Map> response = restTemplate.postForEntity("/payments", request, Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().get("code"));
        assertTrue(response.getBody().get("details").toString().contains("requestedExecutionDate"));
    }
}
