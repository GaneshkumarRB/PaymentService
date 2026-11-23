package com.bnk.paymentservice.repository;

import com.bnk.paymentservice.entity.DeadLetterMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Persists messages that have exhausted their consumer processing attempts.
 */
public interface DeadLetterMessageRepository extends JpaRepository<DeadLetterMessage, UUID> {
}
