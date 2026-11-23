package com.bnk.paymentservice.handler;

import com.bnk.paymentservice.api.model.ErrorResponse;
import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.exception.BadRequestException;
import com.bnk.paymentservice.exception.InternalServerException;
import com.bnk.paymentservice.exception.PaymentNotFoundException;
import com.bnk.paymentservice.exception.PaymentValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Translates request validation and application exceptions into API error responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({
            BadRequestException.class,
            PaymentValidationException.class,
            PaymentNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        ErrorResponse body = new ErrorResponse();
        body.setCode(ex.getCode());
        body.setMessage(ex.getMessage());
        if (ex.getDetails() != null && !ex.getDetails().isEmpty()) {
            body.setDetails(ex.getDetails());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorResponse> handleInternal(InternalServerException ex) {
        log.error("Internal server error [{}]: {}", ex.getCode(), ex.getMessage(), ex);
        ErrorResponse body = new ErrorResponse();
        body.setCode(ex.getCode());
        body.setMessage(AppConstants.MSG_UNEXPECTED_SERVER_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());
        ErrorResponse body = new ErrorResponse();
        body.setCode(AppConstants.CODE_VALIDATION_ERROR);
        body.setMessage(AppConstants.MSG_REQUEST_VALIDATION_FAILED);
        body.setDetails(details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        ErrorResponse body = new ErrorResponse();
        body.setCode(AppConstants.CODE_BAD_REQUEST);
        body.setMessage(AppConstants.MSG_MALFORMED_REQUEST_BODY);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        ErrorResponse body = new ErrorResponse();
        body.setCode(AppConstants.CODE_INTERNAL_ERROR);
        body.setMessage(AppConstants.MSG_UNEXPECTED_SERVER_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
