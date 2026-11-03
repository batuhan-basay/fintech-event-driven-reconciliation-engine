package com.ruvi.reconciliation.infrastructure.audit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "payment_audit_trail")
public class AuditTrailDocument {

    @Id
    private String id;
    private String eventType;
    private String aggregateId;
    private Map<String, Object> payload;
    private Instant receivedAt;

    public AuditTrailDocument() { }

    public AuditTrailDocument(String eventType, String aggregateId, Map<String, Object> payload, Instant receivedAt) {
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.receivedAt = receivedAt;
    }

    public String getId() { return id; }
    public String getEventType() { return eventType; }
    public String getAggregateId() { return aggregateId; }
    public Map<String, Object> getPayload() { return payload; }
    public Instant getReceivedAt() { return receivedAt; }
}
