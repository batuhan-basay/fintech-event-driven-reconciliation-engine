package com.ruvi.reconciliation.api.payment;

import com.ruvi.reconciliation.application.payment.CreatePaymentCommand;
import com.ruvi.reconciliation.application.payment.CreatePaymentHandler;
import com.ruvi.reconciliation.application.payment.IdempotencyException;
import com.ruvi.reconciliation.domain.payment.Payment;
import com.ruvi.reconciliation.infrastructure.persistence.PaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment authorization with idempotency enforcement and transactional outbox")
public class PaymentController {

    private final CreatePaymentHandler handler;
    private final PaymentRepository paymentRepository;

    public PaymentController(CreatePaymentHandler handler, PaymentRepository paymentRepository) {
        this.handler = handler;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    @Operation(summary = "Create a payment", description =
            "Idempotent payment creation. Repeated calls with the same Idempotency-Key return the original response.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment created or replayed",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Concurrent idempotent request in progress")
    })
    public ResponseEntity<PaymentResponse> create(
            @Parameter(description = "Idempotency key echoed by the client", required = true, example = "pay-2026-11-03-0001")
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 80) String idempotencyKey,
            @Valid @RequestBody PaymentRequest body) {

        var result = handler.handle(new CreatePaymentCommand(
                idempotencyKey, body.merchantId(), body.customerId(),
                body.amountMinor(), body.currency()));

        HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(PaymentResponse.of(result.payment(), result.replayed()));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Fetch a payment by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> get(@PathVariable UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .map(p -> ResponseEntity.ok(PaymentResponse.of(p, false)))
                .orElse(ResponseEntity.notFound().build());
    }
}
