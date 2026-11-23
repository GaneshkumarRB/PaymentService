package com.bnk.paymentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds the fraud provider URL, timeouts, and in-process stub setting.
 */
@ConfigurationProperties(prefix = "payment.fraud")
public class FraudClientProperties {

    private String baseUrl = "http://localhost:8080";
    private int connectTimeoutMs = 2000;
    private int readTimeoutMs = 5000;
    private boolean enabledStub = true;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public boolean isEnabledStub() {
        return enabledStub;
    }

    public void setEnabledStub(boolean enabledStub) {
        this.enabledStub = enabledStub;
    }
}
