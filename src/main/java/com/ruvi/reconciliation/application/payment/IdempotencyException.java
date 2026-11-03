package com.ruvi.reconciliation.application.payment;

public class IdempotencyException extends RuntimeException {
    public IdempotencyException(String message) { super(message); }
}
