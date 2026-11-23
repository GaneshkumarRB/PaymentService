package com.bnk.paymentservice.repository;

import com.bnk.paymentservice.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    boolean existsByPaymentId(UUID paymentId);

    Optional<LedgerEntry> findByPaymentId(UUID paymentId);
}
