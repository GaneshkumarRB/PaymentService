package com.bnk.paymentservice.repository;

import com.bnk.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Provides persistence and identifier-based retrieval for payments.
 */
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
