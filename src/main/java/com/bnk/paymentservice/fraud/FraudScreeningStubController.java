package com.bnk.paymentservice.fraud;

import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.exception.InternalServerException;
import com.bnk.paymentservice.config.FraudClientProperties;
import com.bnk.paymentservice.config.FraudStubProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * In-process XML vendor stub. Disable with payment.fraud.enabled-stub=false when pointing at a real provider.
 * <p>
 * Response body is loaded from classpath XML fixtures, selected by payment amount:
 * <ul>
 *   <li>{@code amount >= failureAmountThreshold} → {@code screening_api_failure_response.xml} (HTTP 500)</li>
 *   <li>{@code amount >= riskAmountThreshold} → {@code screening_api_risk_response.xml} (DECLINE)</li>
 *   <li>otherwise → {@code screening_api_response.xml} (ACCEPT)</li>
 * </ul>
 */
@RestController
@ConditionalOnProperty(prefix = "payment.fraud", name = "enabled-stub", havingValue = "true", matchIfMissing = true)
public class FraudScreeningStubController {

    private static final Logger log = LoggerFactory.getLogger(FraudScreeningStubController.class);
    private static final Pattern REQUEST = Pattern.compile(
            "\\s*(?:<\\?xml\\s+[^?]*\\?>\\s*)?<((?:[\\w.-]+:)?)ScreeningRequest(?:\\s+[^<>]*?)?\\s*>.*</\\1ScreeningRequest\\s*>\\s*",
        Pattern.DOTALL);
    private static final Pattern AMOUNT = Pattern.compile(
            "<((?:[\\w.-]+:)?)Amount(?:\\s+[^<>]*?)?\\s*>([^<]+)</\\1Amount\\s*>");

    @Autowired
    private FraudStubProperties properties;
    @Autowired
    private FraudClientProperties clientProperties;

    private String acceptResponseXml;
    private String riskResponseXml;
    private String failureResponseXml;

    @PostConstruct
    public void init() {
        acceptResponseXml = loadClasspathXml(AppConstants.SCREENING_ACCEPT_RESOURCE);
        riskResponseXml = loadClasspathXml(AppConstants.SCREENING_RISK_RESOURCE);
        failureResponseXml = loadClasspathXml(AppConstants.SCREENING_FAILURE_RESOURCE);
        log.info("Fraud screening stub enabled; client base-url={}; amount rules: "
                        + "< {} ACCEPT, >= {} RISK/DECLINE, >= {} FAILURE",
                clientProperties.getBaseUrl(),
                properties.getRiskAmountThreshold(),
                properties.getRiskAmountThreshold(),
                properties.getFailureAmountThreshold());
    }

    @PostMapping(path = "/screening/v2/check",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, MediaType.ALL_VALUE},
            produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> screen(@RequestBody(required = false) String requestXml) throws InterruptedException {
        simulateLatency();
        BigDecimal amount;
        try {
            amount = extractAmount(requestXml);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid fraud screening request: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
        log.debug("Stub screening request amount={}", amount);

        if (amount.compareTo(BigDecimal.valueOf(properties.getFailureAmountThreshold())) >= 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(failureResponseXml);
        }
        if (amount.compareTo(BigDecimal.valueOf(properties.getRiskAmountThreshold())) >= 0) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(riskResponseXml);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(acceptResponseXml);
    }

    private BigDecimal extractAmount(String requestXml) {
        if (requestXml == null || !REQUEST.matcher(requestXml).matches()) {
            throw new IllegalArgumentException("invalid screening request");
        }
        Matcher matcher = AMOUNT.matcher(requestXml);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Amount is missing or invalid");
        }
        try {
            BigDecimal amount = new BigDecimal(matcher.group(2).trim());
            if (amount.signum() <= 0 || matcher.find()) {
                throw new IllegalArgumentException("Amount must be a single positive number");
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("invalid Amount", ex);
        }
    }

    private String loadClasspathXml(String resourceName) {
        ClassPathResource resource = new ClassPathResource(resourceName);
        try (InputStream in = resource.getInputStream()) {
            String xml = StreamUtils.copyToString(in, StandardCharsets.UTF_8).trim();
            if (xml.isEmpty()) {
                throw new InternalServerException(
                        AppConstants.CODE_STUB_FIXTURE_ERROR,
                        AppConstants.MSG_STUB_FIXTURE_EMPTY_PREFIX + resourceName
                );
            }
            return xml;
        } catch (IOException ex) {
            throw new InternalServerException(
                    AppConstants.CODE_STUB_FIXTURE_ERROR,
                    AppConstants.MSG_STUB_FIXTURE_LOAD_PREFIX + resourceName,
                    ex
            );
        }
    }

    private void simulateLatency() throws InterruptedException {
        long min = Math.max(0, properties.getMinLatencyMs());
        long max = Math.max(min, properties.getMaxLatencyMs());
        long sleep = min == max ? min : ThreadLocalRandom.current().nextLong(min, max + 1);
        if (sleep > 0) {
            Thread.sleep(sleep);
        }
    }
}
