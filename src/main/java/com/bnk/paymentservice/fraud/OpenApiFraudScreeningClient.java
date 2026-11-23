package com.bnk.paymentservice.fraud;

import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.exception.FraudVendorException;
import com.bnk.paymentservice.factory.FraudScreeningFactory;
import com.bnk.paymentservice.fraud.api.FraudScreeningApi;
import com.bnk.paymentservice.fraud.api.model.ScreeningRequest;
import com.bnk.paymentservice.fraud.api.model.ScreeningResponse;
import com.bnk.paymentservice.models.FraudScreeningResult;
import com.bnk.paymentservice.models.PaymentSubmittedEvent;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

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
    @Autowired
    private FraudScreeningFactory fraudScreeningFactory;

    @Override
    public FraudScreeningResult screen(PaymentSubmittedEvent event) {
        ScreeningRequest request = fraudScreeningFactory.buildScreeningRequest(event);
        try {
            ScreeningResponse response = fraudScreeningApi.screenPayment(request);
            FraudScreeningResult result = fraudScreeningFactory.buildScreeningResult(response);
            log.debug("Fraud screening decision={} risk={}", result.decision(), result.riskScore());
            return result;
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
                return fraudScreeningFactory.buildVendorException(response.getError(), defaultRetryable);
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

}
