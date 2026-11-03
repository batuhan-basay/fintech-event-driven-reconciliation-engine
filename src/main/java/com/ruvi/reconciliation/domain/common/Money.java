package com.ruvi.reconciliation.domain.common;

import java.util.Objects;

public record Money(long minor, String currency) {
    public Money {
        if (minor < 0) throw new IllegalArgumentException("minor must be non-negative");
        Objects.requireNonNull(currency, "currency");
        if (currency.length() != 3) throw new IllegalArgumentException("currency must be ISO-4217 (3 chars)");
    }
}
