package com.bnk.paymentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fraud-stub")
public class FraudStubProperties {

    /**
     * Amounts at or above this threshold return {@code screening_api_risk_response.xml}.
     * Amounts below return {@code screening_api_response.xml}.
     */
    private double riskAmountThreshold = 1000.0;

    /**
     * Amounts at or above this threshold return {@code screening_api_failure_response.xml}.
     * Takes precedence over {@link #riskAmountThreshold}.
     */
    private double failureAmountThreshold = 5000.0;

    private long minLatencyMs = 50;
    private long maxLatencyMs = 400;

    public double getRiskAmountThreshold() {
        return riskAmountThreshold;
    }

    public void setRiskAmountThreshold(double riskAmountThreshold) {
        this.riskAmountThreshold = riskAmountThreshold;
    }

    public double getFailureAmountThreshold() {
        return failureAmountThreshold;
    }

    public void setFailureAmountThreshold(double failureAmountThreshold) {
        this.failureAmountThreshold = failureAmountThreshold;
    }

    public long getMinLatencyMs() {
        return minLatencyMs;
    }

    public void setMinLatencyMs(long minLatencyMs) {
        this.minLatencyMs = minLatencyMs;
    }

    public long getMaxLatencyMs() {
        return maxLatencyMs;
    }

    public void setMaxLatencyMs(long maxLatencyMs) {
        this.maxLatencyMs = maxLatencyMs;
    }
}
