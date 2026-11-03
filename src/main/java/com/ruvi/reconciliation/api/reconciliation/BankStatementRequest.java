package com.ruvi.reconciliation.api.reconciliation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

@Schema(name = "BankStatementRequest", description = "Simulated external bank statement line")
public record BankStatementRequest(
        @NotBlank @Size(max = 64) String source,
        @NotBlank @Size(max = 80) String externalRef,
        @NotNull LocalDate statementDate,
        @Positive long amountMinor,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NotNull Instant postedAt
) { }
