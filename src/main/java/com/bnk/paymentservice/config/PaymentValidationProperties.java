package com.bnk.paymentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "payment")
public class PaymentValidationProperties {

    private String supportedCurrencies = "USD,EUR,GBP";

    public Set<String> getSupportedCurrencySet() {
        return Arrays.stream(supportedCurrencies.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public String getSupportedCurrencies() {
        return supportedCurrencies;
    }

    public void setSupportedCurrencies(String supportedCurrencies) {
        this.supportedCurrencies = supportedCurrencies;
    }
}
