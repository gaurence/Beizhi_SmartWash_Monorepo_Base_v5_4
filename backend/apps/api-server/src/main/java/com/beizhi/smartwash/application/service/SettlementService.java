package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.infrastructure.repository.InMemoryPlatformRepository;
import com.beizhi.smartwash.interfaces.rest.error.BadRequestException;
import com.beizhi.smartwash.interfaces.rest.error.NotFoundException;
import com.beizhi.smartwash.security.PermissionGuard;
import com.beizhi.smartwash.security.RequestContext;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class SettlementService {
  private static final Set<String> SUPPORTED_EXPORT_FORMATS = Set.of("CSV", "XLSX");

  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public SettlementService(
    InMemoryPlatformRepository repository,
    PermissionGuard permissionGuard,
    IdempotencyService idempotencyService,
    AuditService auditService
  ) {
    this.repository = repository;
    this.permissionGuard = permissionGuard;
    this.idempotencyService = idempotencyService;
    this.auditService = auditService;
  }

  public List<Map<String, Object>> listSettlements(int page, int size) {
    permissionGuard.check("finance.settlement.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.listSettlements(tenantId, page, size);
  }

  public long countSettlements() {
    return repository.countSettlements(RequestContextHolder.get().tenantId());
  }

  public Map<String, Object> getSettlement(String settlementId) {
    permissionGuard.check("finance.settlement.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return settlementInScope(tenantId, settlementId);
  }

  public List<Map<String, Object>> listSettlementLines(String settlementId, int page, int size) {
    permissionGuard.check("finance.settlement.read");
    String tenantId = RequestContextHolder.get().tenantId();
    settlementInScope(tenantId, settlementId);
    return repository.listSettlementLines(tenantId, settlementId, page, size);
  }

  public long countSettlementLines(String settlementId) {
    String tenantId = RequestContextHolder.get().tenantId();
    settlementInScope(tenantId, settlementId);
    return repository.countSettlementLines(tenantId, settlementId);
  }

  public Object exportSettlement(String settlementId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("finance.settlement.export");
    RequestContext context = RequestContextHolder.get();
    settlementInScope(context.tenantId(), settlementId);

    String format = normalizeFormat(body.get("format"));
    String requestHash = context.tenantId() + "|" + settlementId + "|" + format;

    return idempotencyService.execute(
      context.tenantId(),
      "finance-settlement-export",
      idempotencyKey,
      requestHash,
      () -> {
        String exportId = UUID.randomUUID().toString();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(10);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("exportId", exportId);
        result.put("settlementId", settlementId);
        result.put("format", format);
        result.put("status", "READY");
        result.put("downloadUrl", "/exports/settlements/" + exportId + "." + format.toLowerCase());
        result.put("expiresAt", expiresAt.toString());
        result.put("requestId", context.requestId());

        auditService.append(
          "SETTLEMENT_EXPORT_CREATE",
          "settlement:" + settlementId,
          "SUCCESS",
          Map.of(
            "settlementId", settlementId,
            "format", format,
            "exportId", exportId
          )
        );
        return Map.copyOf(result);
      }
    );
  }

  private Map<String, Object> settlementInScope(String tenantId, String settlementId) {
    return repository.findSettlement(tenantId, settlementId)
      .orElseThrow(() -> new NotFoundException("Settlement not found"));
  }

  private String normalizeFormat(Object value) {
    String format = value == null ? "CSV" : String.valueOf(value).trim().toUpperCase();
    if (!StringUtils.hasText(format)) {
      format = "CSV";
    }
    if (!SUPPORTED_EXPORT_FORMATS.contains(format)) {
      throw new BadRequestException("Unsupported export format: " + value);
    }
    return format;
  }
}
