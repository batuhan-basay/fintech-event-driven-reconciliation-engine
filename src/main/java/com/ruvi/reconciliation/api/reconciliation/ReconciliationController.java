package com.ruvi.reconciliation.api.reconciliation;

import com.ruvi.reconciliation.application.reconciliation.ReconciliationJob;
import com.ruvi.reconciliation.domain.reconciliation.BankStatement;
import com.ruvi.reconciliation.domain.reconciliation.Discrepancy;
import com.ruvi.reconciliation.domain.reconciliation.ReconciliationRun;
import com.ruvi.reconciliation.infrastructure.persistence.BankStatementRepository;
import com.ruvi.reconciliation.infrastructure.persistence.DiscrepancyRepository;
import com.ruvi.reconciliation.infrastructure.persistence.ReconciliationRunRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reconciliation")
@Tag(name = "Reconciliation", description = "Nightly reconciliation and discrepancy inspection")
public class ReconciliationController {

    private final ReconciliationJob job;
    private final BankStatementRepository statementRepository;
    private final DiscrepancyRepository discrepancyRepository;
    private final ReconciliationRunRepository runRepository;

    public ReconciliationController(ReconciliationJob job,
                                    BankStatementRepository statementRepository,
                                    DiscrepancyRepository discrepancyRepository,
                                    ReconciliationRunRepository runRepository) {
        this.job = job;
        this.statementRepository = statementRepository;
        this.discrepancyRepository = discrepancyRepository;
        this.runRepository = runRepository;
    }

    @PostMapping("/bank-statements")
    @Operation(summary = "Import a simulated bank-statement line (reconciliation input)")
    public ResponseEntity<Map<String, Object>> importStatement(@Valid @RequestBody BankStatementRequest req) {
        BankStatement saved = statementRepository.save(BankStatement.of(
                req.source(), req.externalRef(), req.statementDate(),
                req.amountMinor(), req.currency(), req.postedAt()));
        return ResponseEntity.ok(Map.of("id", saved.getId(), "externalRef", saved.getExternalRef()));
    }

    @PostMapping("/runs")
    @Operation(summary = "Trigger a reconciliation run for a date (default: yesterday UTC)")
    public ResponseEntity<Map<String, Object>> trigger(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = (date != null) ? date : LocalDate.now().minusDays(1);
        UUID runId = job.runFor(target);
        return ResponseEntity.ok(Map.of("runId", runId, "runDate", target));
    }

    @GetMapping("/runs/{runId}")
    @Operation(summary = "Get reconciliation run details")
    public ResponseEntity<ReconciliationRun> getRun(@PathVariable UUID runId) {
        return runRepository.findById(runId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/runs/{runId}/discrepancies")
    @Operation(summary = "List discrepancies detected in a run")
    public List<Discrepancy> getDiscrepancies(@PathVariable UUID runId) {
        return discrepancyRepository.findByRunId(runId);
    }
}
