package com.bnk.paymentservice.service;

import com.bnk.paymentservice.api.model.PaymentRequest;
import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.config.PaymentValidationProperties;
import com.bnk.paymentservice.exception.PaymentValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates payment amounts, currencies, accounts, references, and execution dates.
 */
@Component
public class PaymentValidator {

    private static final Pattern ACCOUNT_NUMBER = Pattern.compile("^[A-Za-z0-9]{8,34}$");

    @Autowired
    private PaymentValidationProperties properties;
    @Autowired
    private Clock clock;

    public void validate(PaymentRequest request) {
        List<String> details = new ArrayList<>();
        if (request.getAmount() == null || BigDecimal.valueOf(request.getAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            details.add(AppConstants.MSG_AMOUNT_MUST_BE_POSITIVE);
        }
        Set<String> supported = properties.getSupportedCurrencySet();
        if (request.getCurrency() == null || !supported.contains(request.getCurrency().getValue())) {
            details.add(AppConstants.MSG_CURRENCY_MUST_BE_ONE_OF_PREFIX + supported);
        }
        validateAccount(AppConstants.FIELD_SOURCE_ACCOUNT, request.getSourceAccount(), details);
        validateAccount(AppConstants.FIELD_DESTINATION_ACCOUNT, request.getDestinationAccount(), details);
        if (request.getReference() == null || request.getReference().trim().isEmpty()) {
            details.add(AppConstants.MSG_REFERENCE_REQUIRED);
        }
        LocalDate today = LocalDate.now(clock);
        if (request.getRequestedExecutionDate() == null) {
            details.add(AppConstants.MSG_EXECUTION_DATE_REQUIRED);
        } else if (request.getRequestedExecutionDate().isBefore(today)) {
            details.add(AppConstants.MSG_EXECUTION_DATE_NOT_PAST);
        }
        if (!details.isEmpty()) {
            throw PaymentValidationException.invalid(details);
        }
    }

    private void validateAccount(String field, com.bnk.paymentservice.api.model.AccountDetails account, List<String> details) {
        if (account == null) {
            details.add(field + AppConstants.MSG_FIELD_REQUIRED_SUFFIX);
            return;
        }
        if (account.getName() == null || account.getName().trim().isEmpty()) {
            details.add(field + AppConstants.MSG_NAME_REQUIRED_SUFFIX);
        }
        if (account.getAccountNumber() == null || !ACCOUNT_NUMBER.matcher(account.getAccountNumber()).matches()) {
            details.add(field + AppConstants.MSG_ACCOUNT_NUMBER_PATTERN);
        }
    }
}
