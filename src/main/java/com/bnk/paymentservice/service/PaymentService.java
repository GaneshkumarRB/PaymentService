package com.bnk.paymentservice.service;

import com.bnk.paymentservice.api.model.PaymentRequest;
import com.bnk.paymentservice.api.model.PaymentResponse;
import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.entity.OutboxMessage;
import com.bnk.paymentservice.entity.Payment;
import com.bnk.paymentservice.exception.InternalServerException;
import com.bnk.paymentservice.exception.PaymentNotFoundException;
import com.bnk.paymentservice.factory.PaymentFactory;
import com.bnk.paymentservice.models.PaymentSubmittedEvent;
import com.bnk.paymentservice.repository.OutboxMessageRepository;
import com.bnk.paymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Validates and stores payment submissions with an outbox event, and retrieves payments.
 */
@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OutboxMessageRepository outboxMessageRepository;
    @Autowired
    private PaymentValidator paymentValidator;
    @Autowired
    private PaymentFactory paymentFactory;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Clock clock;

    @Transactional
    public PaymentResponse submit(PaymentRequest request) {
        paymentValidator.validate(request);
        Instant now = clock.instant();
        UUID paymentId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Payment payment = paymentFactory.buildPayment(request, paymentId, now);
        paymentRepository.save(payment);

        PaymentSubmittedEvent event = new PaymentSubmittedEvent(
                eventId,
                paymentId,
                payment.getSourceAccount().getName(),
                payment.getSourceAccount().getAccountNumber(),
                payment.getDestinationAccount().getName(),
                payment.getDestinationAccount().getAccountNumber(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getReference(),
                payment.getRequestedExecutionDate(),
                now
        );
        try {
            outboxMessageRepository.save(new OutboxMessage(
                    eventId,
                    AppConstants.PAYMENT_SUBMITTED,
                    paymentId,
                    objectMapper.writeValueAsString(event),
                    now
            ));
        } catch (JsonProcessingException e) {
            throw new InternalServerException(
                    AppConstants.CODE_SERIALIZATION_ERROR,
                    AppConstants.MSG_UNABLE_TO_SERIALIZE_PAYMENT_SUBMITTED,
                    e
            );
        }
        return paymentFactory.buildPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return paymentFactory.buildPaymentResponse(payment);
    }
}
