package com.ruvi.reconciliation.infrastructure.persistence;

import com.ruvi.reconciliation.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByMerchantIdAndIdempotencyKey(String merchantId, String idempotencyKey);
    Optional<Payment> findByExternalRef(String externalRef);
}
