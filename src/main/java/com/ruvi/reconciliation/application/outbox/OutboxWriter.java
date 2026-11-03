package com.ruvi.reconciliation.application.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruvi.reconciliation.domain.outbox.OutboxEvent;
import com.ruvi.reconciliation.infrastructure.persistence.OutboxEventRepository;
import org.springframework.stereotype.Component;

@Component
public class OutboxWriter {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxWriter(OutboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public OutboxEvent write(String aggregateType, String aggregateId, String eventType, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            return repository.save(OutboxEvent.create(aggregateType, aggregateId, eventType, json));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }
}
