package com.ruvi.reconciliation.application.reconciliation;

import com.ruvi.reconciliation.domain.payment.Payment;
import com.ruvi.reconciliation.domain.reconciliation.BankStatement;
import com.ruvi.reconciliation.domain.reconciliation.Discrepancy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DiscrepancyDetector {

    public static Result detect(UUID runId, List<Payment> payments, List<BankStatement> statements) {
        Map<String, Payment> paymentsByRef = new HashMap<>();
        for (Payment p : payments) {
            if (p.getExternalRef() != null) paymentsByRef.put(p.getExternalRef(), p);
        }

        List<Discrepancy> discrepancies = new ArrayList<>();
        int matched = 0;

        for (BankStatement s : statements) {
            Payment p = paymentsByRef.remove(s.getExternalRef());
            if (p == null) {
                discrepancies.add(Discrepancy.missingInLedger(runId, s.getId(), s.getAmountMinor(), s.getCurrency()));
                continue;
            }
            if (p.getAmountMinor() != s.getAmountMinor() || !p.getCurrency().equals(s.getCurrency())) {
                discrepancies.add(Discrepancy.amountMismatch(runId, p.getId(), s.getId(),
                        p.getAmountMinor(), s.getAmountMinor(), s.getCurrency()));
                continue;
            }
            matched++;
        }

        for (Payment orphan : paymentsByRef.values()) {
            discrepancies.add(Discrepancy.missingInStatement(runId, orphan.getId(),
                    orphan.getAmountMinor(), orphan.getCurrency()));
        }

        return new Result(matched, discrepancies);
    }

    public record Result(int matched, List<Discrepancy> discrepancies) { }

    private DiscrepancyDetector() { }
}
