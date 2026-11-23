package com.bnk.paymentservice.factory;

import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.exception.FraudVendorException;
import com.bnk.paymentservice.fraud.api.model.Error;
import com.bnk.paymentservice.fraud.api.model.Reason;
import com.bnk.paymentservice.fraud.api.model.ScreeningRequest;
import com.bnk.paymentservice.fraud.api.model.ScreeningResponse;
import com.bnk.paymentservice.fraud.api.model.ScreeningResult;
import com.bnk.paymentservice.models.FraudDecisionType;
import com.bnk.paymentservice.models.FraudScreeningResult;
import com.bnk.paymentservice.models.PaymentSubmittedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class FraudScreeningFactory {

    public ScreeningRequest buildScreeningRequest(PaymentSubmittedEvent event) {
        return new ScreeningRequest()
                .paymentId(event.paymentId())
                .amount(event.amount().doubleValue())
                .currency(event.currency())
                .sourceAccountNumber(event.sourceAccountNumber())
                .destinationAccountNumber(event.destinationAccountNumber())
                .reference(event.reference());
    }

    public FraudScreeningResult buildScreeningResult(ScreeningResponse response) {
        if (response == null) {
            throw new FraudVendorException(
                    AppConstants.CODE_EMPTY_RESPONSE,
                    AppConstants.MSG_EMPTY_SCREENING_RESPONSE,
                    true
            );
        }
        if (response.getError() != null) {
            throw buildVendorException(response.getError(), false);
        }
        ScreeningResult result = response.getResult();
        if (result == null || result.getDecision() == null || result.getRiskScore() == null) {
            throw new FraudVendorException(
                    AppConstants.CODE_MALFORMED_RESPONSE,
                    AppConstants.MSG_MISSING_DECISION_RISK,
                    true
            );
        }
        return new FraudScreeningResult(
                FraudDecisionType.valueOf(result.getDecision().getValue()),
                result.getRiskScore(),
                buildReasonTexts(result.getReasons())
        );
    }

    public FraudVendorException buildVendorException(Error error, boolean defaultRetryable) {
        String code = error.getCode() == null ? AppConstants.CODE_VENDOR_ERROR : error.getCode();
        String message = error.getMessage() == null
                ? AppConstants.MSG_VENDOR_SCREENING_FAILED
                : error.getMessage();
        boolean retryable = error.getRetryable() == null ? defaultRetryable : error.getRetryable();
        return new FraudVendorException(code, message, retryable);
    }

    private List<String> buildReasonTexts(List<Reason> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> texts = new ArrayList<String>();
        for (Reason reason : reasons) {
            if (reason != null && reason.getValue() != null) {
                texts.add(reason.getValue());
            }
        }
        return texts;
    }
}