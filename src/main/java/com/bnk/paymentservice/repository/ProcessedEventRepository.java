package com.bnk.paymentservice.repository;

import com.bnk.paymentservice.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Tracks events processed by each consumer for duplicate detection.
 */
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, ProcessedEvent.ProcessedEventId> {

    boolean existsByConsumerNameAndEventId(String consumerName, UUID eventId);
}
