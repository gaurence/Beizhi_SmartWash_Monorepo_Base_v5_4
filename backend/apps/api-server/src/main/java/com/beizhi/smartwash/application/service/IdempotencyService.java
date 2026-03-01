package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.IdempotencyRecord;
import com.beizhi.smartwash.infrastructure.repository.InMemoryPlatformRepository;
import com.beizhi.smartwash.interfaces.rest.error.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
public class IdempotencyService {
  private final InMemoryPlatformRepository repository;
  private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

  public IdempotencyService(InMemoryPlatformRepository repository) {
    this.repository = repository;
  }

  public Object execute(String tenantId, String scope, String idempotencyKey, String requestHash, Supplier<Object> supplier) {
    if (!StringUtils.hasText(idempotencyKey)) {
      return supplier.get();
    }

    String compositeKey = tenantId + "::" + scope + "::" + idempotencyKey;
    ReentrantLock lock = locks.computeIfAbsent(compositeKey, ignore -> new ReentrantLock());

    lock.lock();
    try {
      IdempotencyRecord existing = repository.findIdempotency(compositeKey).orElse(null);
      if (existing != null) {
        if (!existing.requestHash().equals(requestHash)) {
          throw new ConflictException("Idempotency-Key reused with different payload");
        }
        return existing.response();
      }

      Object response = supplier.get();
      repository.saveIdempotency(new IdempotencyRecord(
        compositeKey,
        tenantId,
        requestHash,
        response,
        OffsetDateTime.now()
      ));
      return response;
    } finally {
      lock.unlock();
    }
  }
}
