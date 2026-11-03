package com.ruvi.reconciliation.application.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {

    private static final String KEY_PREFIX = "idemp:payment:";

    private final StringRedisTemplate redis;
    private final Duration ttl;
    private final Duration lockTimeout;

    public IdempotencyService(StringRedisTemplate redis,
                              @Value("${reconciliation.idempotency.ttl:PT24H}") Duration ttl,
                              @Value("${reconciliation.idempotency.lock-timeout:PT3S}") Duration lockTimeout) {
        this.redis = redis;
        this.ttl = ttl;
        this.lockTimeout = lockTimeout;
    }

    public boolean tryAcquire(String merchantId, String idempotencyKey) {
        Boolean ok = redis.opsForValue().setIfAbsent(key(merchantId, idempotencyKey), "LOCKED", lockTimeout);
        return Boolean.TRUE.equals(ok);
    }

    public void commit(String merchantId, String idempotencyKey, String paymentId) {
        redis.opsForValue().set(key(merchantId, idempotencyKey), paymentId, ttl);
    }

    public void release(String merchantId, String idempotencyKey) {
        redis.delete(key(merchantId, idempotencyKey));
    }

    private String key(String merchantId, String idempotencyKey) {
        return KEY_PREFIX + merchantId + ":" + idempotencyKey;
    }
}
