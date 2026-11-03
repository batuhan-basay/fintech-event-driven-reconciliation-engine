package com.ruvi.reconciliation.api.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(name = "PaymentRequest", description = "Create a new payment authorization")
public record PaymentRequest(
        @Schema(example = "MERCHANT_42", description = "Merchant initiating the payment")
        @NotBlank @Size(max = 64) String merchantId,

        @Schema(example = "CUST_9001")
        @NotBlank @Size(max = 64) String customerId,

        @Schema(example = "125000", description = "Amount in minor units (e.g. kuruş)")
        @Positive long amountMinor,

        @Schema(example = "TRY", description = "ISO-4217 currency code")
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency
) { }
