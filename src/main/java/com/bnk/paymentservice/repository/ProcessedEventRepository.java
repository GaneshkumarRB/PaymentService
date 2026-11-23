package com.bnk.paymentservice.repository;

import com.bnk.paymentservice.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, ProcessedEvent.ProcessedEventId> {

    boolean existsByConsumerNameAndEventId(String consumerName, UUID eventId);
}
