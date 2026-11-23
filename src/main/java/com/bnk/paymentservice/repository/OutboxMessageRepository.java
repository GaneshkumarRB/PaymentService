package com.bnk.paymentservice.repository;

import com.bnk.paymentservice.entity.OutboxMessage;
import com.bnk.paymentservice.models.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {

    List<OutboxMessage> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);
}
