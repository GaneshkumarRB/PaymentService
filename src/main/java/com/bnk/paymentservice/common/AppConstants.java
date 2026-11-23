package com.bnk.paymentservice.common;

/**
 * Shared identifiers, error codes, messages, and resource names used by the service.
 */
public final class AppConstants {

    private AppConstants() {
    }

    // --- Event / messaging ---
    public static final String PAYMENT_SUBMITTED = "PaymentSubmitted";
    public static final String CONSUMER_FRAUD_SCREENING = "fraud-screening";
    public static final String CONSUMER_LEDGER = "ledger";
    public static final String CONSUMER_NOTIFICATION = "notification";
    public static final String BROKER_WORKER_THREAD_PREFIX = "payment-broker-worker-";
    public static final String UNKNOWN_ERROR = "unknown error";

    // --- Fraud stub classpath fixtures ---
    public static final String SCREENING_ACCEPT_RESOURCE = "screening_api_response.xml";
    public static final String SCREENING_RISK_RESOURCE = "screening_api_risk_response.xml";
    public static final String SCREENING_FAILURE_RESOURCE = "screening_api_failure_response.xml";

    // --- Error codes ---
    public static final String CODE_VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String CODE_BAD_REQUEST = "BAD_REQUEST";
    public static final String CODE_NOT_FOUND = "NOT_FOUND";
    public static final String CODE_INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String CODE_VENDOR_TRANSPORT_ERROR = "VENDOR_TRANSPORT_ERROR";
    public static final String CODE_EMPTY_RESPONSE = "EMPTY_RESPONSE";
    public static final String CODE_MALFORMED_RESPONSE = "MALFORMED_RESPONSE";
    public static final String CODE_VENDOR_ERROR = "VENDOR_ERROR";
    public static final String CODE_PAYMENT_MISSING = "PAYMENT_MISSING";
    public static final String CODE_SERIALIZATION_ERROR = "SERIALIZATION_ERROR";
    public static final String CODE_OPTIMISTIC_LOCK = "OPTIMISTIC_LOCK_FAILURE";
    public static final String CODE_STUB_FIXTURE_ERROR = "STUB_FIXTURE_ERROR";

    // --- Error / validation messages ---
    public static final String MSG_PAYMENT_REQUEST_INVALID = "Payment request is invalid";
    public static final String MSG_REQUEST_VALIDATION_FAILED = "Request validation failed";
    public static final String MSG_MALFORMED_REQUEST_BODY = "Malformed request body";
    public static final String MSG_UNEXPECTED_SERVER_ERROR = "Unexpected server error";
    public static final String MSG_PAYMENT_NOT_FOUND_PREFIX = "Payment not found: ";
    public static final String MSG_PAYMENT_MISSING_PREFIX = "Payment missing: ";
    public static final String MSG_PAYMENT_MISSING_FOR_SCREENING_PREFIX = "Payment missing for screening: ";
    public static final String MSG_UNABLE_TO_SERIALIZE_PAYMENT_SUBMITTED =
            "Unable to serialize PaymentSubmitted event";
    public static final String MSG_EMPTY_SCREENING_RESPONSE = "Empty screening response";
    public static final String MSG_EMPTY_SCREENING_ERROR_BODY = "Empty screening error body";
    public static final String MSG_MISSING_DECISION_RISK = "Missing decision/risk score";
    public static final String MSG_VENDOR_SCREENING_FAILED = "Vendor screening failed";
    public static final String MSG_STUB_FIXTURE_EMPTY_PREFIX = "Classpath fraud stub fixture is empty: ";
    public static final String MSG_STUB_FIXTURE_LOAD_PREFIX = "Unable to load fraud stub fixture: ";
    public static final String MSG_OPTIMISTIC_LOCK_EXHAUSTED =
            "Unable to update payment after optimistic lock retries";

    public static final String MSG_AMOUNT_MUST_BE_POSITIVE = "amount must be positive";
    public static final String MSG_CURRENCY_MUST_BE_ONE_OF_PREFIX = "currency must be one of ";
    public static final String MSG_REFERENCE_REQUIRED = "reference is required";
    public static final String MSG_EXECUTION_DATE_REQUIRED = "requestedExecutionDate is required";
    public static final String MSG_EXECUTION_DATE_NOT_PAST = "requestedExecutionDate must not be in the past";
    public static final String MSG_FIELD_REQUIRED_SUFFIX = " is required";
    public static final String MSG_NAME_REQUIRED_SUFFIX = ".name is required";
    public static final String MSG_ACCOUNT_NUMBER_PATTERN =
            ".accountNumber must match ^[A-Za-z0-9]{8,34}$";

    public static final String FIELD_SOURCE_ACCOUNT = "sourceAccount";
    public static final String FIELD_DESTINATION_ACCOUNT = "destinationAccount";

    public static final String DEFAULT_FRAUD_BASE_URL = "http://localhost:8080";
    public static final int MAX_OPTIMISTIC_LOCK_ATTEMPTS = 5;
}
