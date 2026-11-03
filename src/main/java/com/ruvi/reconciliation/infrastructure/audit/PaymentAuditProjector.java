package com.ruvi.reconciliation.infrastructure.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class PaymentAuditProjector {

    private static final Logger log = LoggerFactory.getLogger(PaymentAuditProjector.class);

    private final AuditTrailRepository repository;
    private final ObjectMapper objectMapper;
    private final MeterRegistry metrics;

    public PaymentAuditProjector(AuditTrailRepository repository, ObjectMapper objectMapper, MeterRegistry metrics) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }

    @RabbitListener(queues = "${reconciliation.rabbit.payments-queue}")
    public void onPaymentEvent(Message message) {
        try {
            String eventType = (String) message.getMessageProperties().getHeaders().getOrDefault("eventType", "unknown");
            String aggregateId = (String) message.getMessageProperties().getHeaders().getOrDefault("aggregateId", "unknown");
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(message.getBody(), Map.class);
            repository.save(new AuditTrailDocument(eventType, aggregateId, payload, Instant.now()));
            metrics.counter("audit.events").increment();
        } catch (Exception e) {
            log.error("Failed to project payment event to audit trail", e);
            throw new RuntimeException(e);
        }
    }
}
