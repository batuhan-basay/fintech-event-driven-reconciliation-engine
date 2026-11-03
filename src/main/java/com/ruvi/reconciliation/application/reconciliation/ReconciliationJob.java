package com.ruvi.reconciliation.application.reconciliation;

import com.ruvi.reconciliation.domain.reconciliation.ReconciliationRun;
import com.ruvi.reconciliation.infrastructure.persistence.BankStatementRepository;
import com.ruvi.reconciliation.infrastructure.persistence.DiscrepancyRepository;
import com.ruvi.reconciliation.infrastructure.persistence.PaymentRepository;
import com.ruvi.reconciliation.infrastructure.persistence.ReconciliationRunRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class ReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationJob.class);

    private final PaymentRepository paymentRepository;
    private final BankStatementRepository bankStatementRepository;
    private final ReconciliationRunRepository runRepository;
    private final DiscrepancyRepository discrepancyRepository;
    private final MeterRegistry metrics;

    public ReconciliationJob(PaymentRepository paymentRepository,
                             BankStatementRepository bankStatementRepository,
                             ReconciliationRunRepository runRepository,
                             DiscrepancyRepository discrepancyRepository,
                             MeterRegistry metrics) {
        this.paymentRepository = paymentRepository;
        this.bankStatementRepository = bankStatementRepository;
        this.runRepository = runRepository;
        this.discrepancyRepository = discrepancyRepository;
        this.metrics = metrics;
    }

    @Scheduled(cron = "${reconciliation.job.cron:0 15 2 * * *}", zone = "UTC")
    public UUID runNightly() {
        return runFor(LocalDate.now(ZoneOffset.UTC).minusDays(1));
    }

    @Transactional
    public UUID runFor(LocalDate date) {
        ReconciliationRun run = runRepository.save(ReconciliationRun.start(date));
        try {
            var payments = paymentRepository.findAll();
            var statements = bankStatementRepository.findByStatementDate(date);
            var result = DiscrepancyDetector.detect(run.getId(), payments, statements);
            discrepancyRepository.saveAll(result.discrepancies());
            run.complete(result.matched(), result.discrepancies().size());
            runRepository.save(run);
            metrics.counter("reconciliation.runs").increment();
            metrics.counter("reconciliation.discrepancies").increment(result.discrepancies().size());
            log.info("Reconciliation {} for {}: matched={}, discrepancies={}",
                    run.getId(), date, result.matched(), result.discrepancies().size());
            return run.getId();
        } catch (RuntimeException e) {
            run.fail();
            runRepository.save(run);
            throw e;
        }
    }
}
