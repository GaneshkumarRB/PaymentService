package com.bnk.paymentservice.fraud;

import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.exception.FraudVendorException;
import com.bnk.paymentservice.fraud.api.FraudScreeningApi;
import com.bnk.paymentservice.fraud.api.model.Error;
import com.bnk.paymentservice.fraud.api.model.Reason;
import com.bnk.paymentservice.fraud.api.model.ScreeningRequest;
import com.bnk.paymentservice.fraud.api.model.ScreeningResponse;
import com.bnk.paymentservice.fraud.api.model.ScreeningResult;
import com.bnk.paymentservice.models.FraudDecisionType;
import com.bnk.paymentservice.models.FraudScreeningResult;
import com.bnk.paymentservice.models.PaymentSubmittedEvent;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain adapter over the OpenAPI-generated {@link FraudScreeningApi}.
 * HTTP/XML transport is owned by the generated RestTemplate client.
 */
@Component
public class OpenApiFraudScreeningClient implements FraudScreeningClient {

    private static final Logger log = LoggerFactory.getLogger(OpenApiFraudScreeningClient.class);

    @Autowired
    private FraudScreeningApi fraudScreeningApi;
    @Autowired
    private XmlMapper fraudXmlMapper;

    @Override
    public FraudScreeningResult screen(PaymentSubmittedEvent event) {
        ScreeningRequest request = new ScreeningRequest()
                .paymentId(event.paymentId())
                .amount(event.amount().doubleValue())
                .currency(event.currency())
                .sourceAccountNumber(event.sourceAccountNumber())
                .destinationAccountNumber(event.destinationAccountNumber())
                .reference(event.reference());
        try {
            ScreeningResponse response = fraudScreeningApi.screenPayment(request);
            return toDomainResult(response);
        } catch (HttpStatusCodeException ex) {
            throw toVendorException(ex.getResponseBodyAsString(), true);
        } catch (RestClientException ex) {
            throw new FraudVendorException(
                    AppConstants.CODE_VENDOR_TRANSPORT_ERROR,
                    ex.getMessage(),
                    true
            );
        }
    }

    private FraudScreeningResult toDomainResult(ScreeningResponse response) {
        if (response == null) {
            throw new FraudVendorException(
                    AppConstants.CODE_EMPTY_RESPONSE,
                    AppConstants.MSG_EMPTY_SCREENING_RESPONSE,
                    true
            );
        }
        if (response.getError() != null) {
            throw fromError(response.getError(), false);
        }
        ScreeningResult result = response.getResult();
        if (result == null || result.getDecision() == null || result.getRiskScore() == null) {
            throw new FraudVendorException(
                    AppConstants.CODE_MALFORMED_RESPONSE,
                    AppConstants.MSG_MISSING_DECISION_RISK,
                    true
            );
        }
        FraudDecisionType decision = FraudDecisionType.valueOf(result.getDecision().getValue());
        List<String> reasons = toReasonTexts(result.getReasons());
        log.debug("Fraud screening decision={} risk={}", decision, result.getRiskScore());
        return new FraudScreeningResult(decision, result.getRiskScore(), reasons);
    }

    private List<String> toReasonTexts(List<Reason> reasons) {
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

    private FraudVendorException toVendorException(String xml, boolean defaultRetryable) {
        if (xml == null || xml.trim().isEmpty()) {
            return new FraudVendorException(
                    AppConstants.CODE_EMPTY_RESPONSE,
                    AppConstants.MSG_EMPTY_SCREENING_ERROR_BODY,
                    defaultRetryable
            );
        }
        try {
            ScreeningResponse response = fraudXmlMapper.readValue(xml, ScreeningResponse.class);
            if (response.getError() != null) {
                return fromError(response.getError(), defaultRetryable);
            }
        } catch (Exception ignored) {
            // fall through to generic error
        }
        return new FraudVendorException(
                AppConstants.CODE_VENDOR_ERROR,
                AppConstants.MSG_VENDOR_SCREENING_FAILED,
                defaultRetryable
        );
    }

    private FraudVendorException fromError(Error error, boolean defaultRetryable) {
        String code = error.getCode() == null ? AppConstants.CODE_VENDOR_ERROR : error.getCode();
        String message = error.getMessage() == null
                ? AppConstants.MSG_VENDOR_SCREENING_FAILED
                : error.getMessage();
        boolean retryable = error.getRetryable() == null ? defaultRetryable : error.getRetryable();
        return new FraudVendorException(code, message, retryable);
    }
}
