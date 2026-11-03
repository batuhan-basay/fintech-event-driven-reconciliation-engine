package com.ruvi.reconciliation.application.outbox;

import com.ruvi.reconciliation.domain.outbox.OutboxEvent;
import com.ruvi.reconciliation.domain.outbox.OutboxStatus;
import com.ruvi.reconciliation.infrastructure.messaging.RabbitEventPublisher;
import com.ruvi.reconciliation.infrastructure.persistence.OutboxEventRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class OutboxDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

    private final OutboxEventRepository repository;
    private final RabbitEventPublisher publisher;
    private final MeterRegistry metrics;
    private final int batchSize;
    private final int maxAttempts;

    public OutboxDispatcher(OutboxEventRepository repository,
                            RabbitEventPublisher publisher,
                            MeterRegistry metrics,
                            @Value("${reconciliation.outbox.batch-size:100}") int batchSize,
                            @Value("${reconciliation.outbox.max-attempts:10}") int maxAttempts) {
        this.repository = repository;
        this.publisher = publisher;
        this.metrics = metrics;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${reconciliation.outbox.poll-interval:PT1S}")
    @Transactional
    public void dispatchBatch() {
        List<OutboxEvent> batch = repository.lockBatchForDispatch(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED),
                Instant.now(),
                PageRequest.of(0, batchSize));
        if (batch.isEmpty()) return;

        for (OutboxEvent event : batch) {
            try {
                event.markDispatching();
                publisher.publish(event.getEventType(), event.getEventType(),
                        event.getAggregateId(), event.getPayload());
                event.markPublished();
                metrics.counter("outbox.published").increment();
            } catch (Exception e) {
                long delaySeconds = Math.min(300, (long) Math.pow(2, event.getAttempts() + 1));
                event.markFailed(e.getMessage(), Instant.now().plus(Duration.ofSeconds(delaySeconds)));
                metrics.counter("outbox.failed").increment();
                log.warn("Outbox event {} failed, attempt {}/{}: {}",
                        event.getId(), event.getAttempts(), maxAttempts, e.getMessage());
            }
        }
    }
}
