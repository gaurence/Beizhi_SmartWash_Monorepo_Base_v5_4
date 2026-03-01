package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.AuditService;
import com.beizhi.smartwash.common.ApiResponse;
import com.beizhi.smartwash.common.PageResponse;
import com.beizhi.smartwash.domain.model.AuditLog;
import com.beizhi.smartwash.interfaces.rest.error.NotFoundException;
import com.beizhi.smartwash.security.PermissionGuard;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuditController {
  private final AuditService auditService;
  private final PermissionGuard permissionGuard;

  public AuditController(AuditService auditService, PermissionGuard permissionGuard) {
    this.auditService = auditService;
    this.permissionGuard = permissionGuard;
  }

  @GetMapping("/audit/logs")
  public PageResponse listAuditLogs(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    permissionGuard.check("audit.log.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return PageResponse.of(auditService.list(tenantId, page, size), page, size, auditService.count(tenantId));
  }

  @GetMapping("/audit/logs/{logId}")
  public ApiResponse getAuditLog(@PathVariable("logId") String logId) {
    permissionGuard.check("audit.log.read");
    String tenantId = RequestContextHolder.get().tenantId();
    AuditLog log = auditService.getById(tenantId, logId);
    if (log == null) {
      throw new NotFoundException("Audit log not found");
    }
    return ApiResponse.ok(log);
  }

  @PostMapping("/audit/exports")
  public ApiResponse exportAuditLogs(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    permissionGuard.check("audit.export");
    return ApiResponse.ok(auditService.createExportJob(body == null ? Map.of() : body, idempotencyKey));
  }
}
