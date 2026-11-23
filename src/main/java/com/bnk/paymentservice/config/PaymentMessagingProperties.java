package com.bnk.paymentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds broker worker and retry settings for payment event processing.
 */
@ConfigurationProperties(prefix = "payment.messaging")
public class PaymentMessagingProperties {

    private int workerThreads = 4;
    private int maxAttempts = 5;
    private long initialBackoffMs = 200;
    private long maxBackoffMs = 5000;
    private double backoffMultiplier = 2.0;

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public long getInitialBackoffMs() {
        return initialBackoffMs;
    }

    public void setInitialBackoffMs(long initialBackoffMs) {
        this.initialBackoffMs = initialBackoffMs;
    }

    public long getMaxBackoffMs() {
        return maxBackoffMs;
    }

    public void setMaxBackoffMs(long maxBackoffMs) {
        this.maxBackoffMs = maxBackoffMs;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public void setBackoffMultiplier(double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }
}
