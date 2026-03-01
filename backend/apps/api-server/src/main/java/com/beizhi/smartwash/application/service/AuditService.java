package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.AuditLog;
import com.beizhi.smartwash.domain.model.AuditExportJob;
import com.beizhi.smartwash.infrastructure.repository.InMemoryPlatformRepository;
import com.beizhi.smartwash.security.RequestContext;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {
  private final InMemoryPlatformRepository repository;
  private final IdempotencyService idempotencyService;

  public AuditService(InMemoryPlatformRepository repository, IdempotencyService idempotencyService) {
    this.repository = repository;
    this.idempotencyService = idempotencyService;
  }

  public void append(String action, String resource, String result, Map<String, Object> details) {
    RequestContext context = RequestContextHolder.get();
    repository.appendAudit(new AuditLog(
      UUID.randomUUID().toString(),
      context.requestId(),
      context.tenantId(),
      context.userId(),
      action,
      resource,
      result,
      context.clientIp(),
      context.userAgent(),
      OffsetDateTime.now(),
      details
    ));
  }

  public List<AuditLog> list(String tenantId, int page, int size) {
    return repository.listAudit(tenantId, page, size);
  }

  public long count(String tenantId) {
    return repository.countAudit(tenantId);
  }

  public AuditLog getById(String tenantId, String logId) {
    return repository.findAudit(tenantId, logId).orElse(null);
  }

  public Object createExportJob(Map<String, Object> body, String idempotencyKey) {
    RequestContext context = RequestContextHolder.get();
    String requestHash = "audit-export|" + String.valueOf(body);

    return idempotencyService.execute(
      context.tenantId(),
      "audit-export",
      idempotencyKey,
      requestHash,
      () -> {
        String exportId = UUID.randomUUID().toString();
        AuditExportJob job = new AuditExportJob(
          exportId,
          context.tenantId(),
          context.requestId(),
          context.userId(),
          "READY",
          "/exports/audit/" + exportId + ".csv",
          OffsetDateTime.now(),
          body
        );
        repository.saveAuditExport(job);
        append(
          "AUDIT_EXPORT_CREATE",
          "audit-export:" + exportId,
          "SUCCESS",
          Map.of("filters", body)
        );
        return job;
      }
    );
  }
}
