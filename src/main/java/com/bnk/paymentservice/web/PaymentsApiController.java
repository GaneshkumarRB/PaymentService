package com.bnk.paymentservice.web;

import com.bnk.paymentservice.api.PaymentsApi;
import com.bnk.paymentservice.api.model.PaymentRequest;
import com.bnk.paymentservice.api.model.PaymentResponse;
import com.bnk.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Implements the payment submission and retrieval endpoints.
 */
@RestController
public class PaymentsApiController implements PaymentsApi {

    @Autowired
    private PaymentService paymentService;

    @Override
    public ResponseEntity<PaymentResponse> submitPayment(PaymentRequest paymentRequest) {
        PaymentResponse response = paymentService.submit(paymentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<PaymentResponse> getPayment(UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(paymentService.get(id));
    }
}
