package com.ruvi.reconciliation.infrastructure.audit;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AuditTrailRepository extends MongoRepository<AuditTrailDocument, String> {
    List<AuditTrailDocument> findByAggregateIdOrderByReceivedAtDesc(String aggregateId);
}
