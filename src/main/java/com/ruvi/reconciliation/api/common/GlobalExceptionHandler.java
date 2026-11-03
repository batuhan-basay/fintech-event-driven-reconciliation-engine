package com.ruvi.reconciliation.api.common;

import com.ruvi.reconciliation.application.payment.IdempotencyException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> onValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fieldErrorToString).toList();
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("validation_failed", "Request validation failed", details));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> onParamValidation(HandlerMethodValidationException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("validation_failed", ex.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> onMissingHeader(MissingRequestHeaderException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("missing_header", "Required header is missing: " + ex.getHeaderName()));
    }

    @ExceptionHandler(IdempotencyException.class)
    public ResponseEntity<ErrorResponse> onIdempotency(IdempotencyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("idempotency_conflict", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> onDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("data_integrity",
                        "A unique constraint prevented this operation; the row probably already exists."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> onIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("bad_request", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> onUnhandled(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of("internal_error",
                        "An unexpected error occurred: " + ex.getClass().getSimpleName()));
    }

    private String fieldErrorToString(FieldError e) {
        return e.getField() + ": " + e.getDefaultMessage();
    }
}
