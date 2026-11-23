package com.bnk.paymentservice.service;

import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.exception.InternalServerException;
import com.bnk.paymentservice.models.FraudDecisionType;
import com.bnk.paymentservice.entity.Payment;
import com.bnk.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Applies fraud and ledger updates using transactions with optimistic-lock retries.
 */
@Service
public class PaymentUpdateService {

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @PostConstruct
    public void init() {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(
                org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
    }

    public void applyFraudDecisionWithRetry(UUID paymentId,
                                            FraudDecisionType decision,
                                            Integer riskScore,
                                            List<String> reasons,
                                            Instant screenedAt) {
        ObjectOptimisticLockingFailureException last = null;
        for (int attempt = 1; attempt <= AppConstants.MAX_OPTIMISTIC_LOCK_ATTEMPTS; attempt++) {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    Payment payment = paymentRepository.findById(paymentId)
                            .orElseThrow(() -> new InternalServerException(
                                    AppConstants.CODE_PAYMENT_MISSING,
                                    AppConstants.MSG_PAYMENT_MISSING_PREFIX + paymentId
                            ));
                    if (payment.getFraudDecision() != null) {
                        return;
                    }
                    payment.applyFraudDecision(decision, riskScore, reasons, screenedAt);
                    paymentRepository.saveAndFlush(payment);
                });
                return;
            } catch (ObjectOptimisticLockingFailureException ex) {
                last = ex;
            }
        }
        throw new InternalServerException(
                AppConstants.CODE_OPTIMISTIC_LOCK,
                AppConstants.MSG_OPTIMISTIC_LOCK_EXHAUSTED,
                last
        );
    }

    public void markLedgerPostedWithRetry(UUID paymentId) {
        ObjectOptimisticLockingFailureException last = null;
        for (int attempt = 1; attempt <= AppConstants.MAX_OPTIMISTIC_LOCK_ATTEMPTS; attempt++) {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    Payment payment = paymentRepository.findById(paymentId).orElse(null);
                    if (payment == null || payment.isLedgerPosted()) {
                        return;
                    }
                    payment.markLedgerPosted();
                    paymentRepository.saveAndFlush(payment);
                });
                return;
            } catch (ObjectOptimisticLockingFailureException ex) {
                last = ex;
            }
        }
        if (last != null) {
            throw new InternalServerException(
                    AppConstants.CODE_OPTIMISTIC_LOCK,
                    AppConstants.MSG_OPTIMISTIC_LOCK_EXHAUSTED,
                    last
            );
        }
    }
}
