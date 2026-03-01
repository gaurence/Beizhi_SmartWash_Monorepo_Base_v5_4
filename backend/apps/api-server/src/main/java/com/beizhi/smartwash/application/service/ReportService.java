package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.ReportExportJob;
import com.beizhi.smartwash.domain.model.ReportSnapshot;
import com.beizhi.smartwash.infrastructure.repository.InMemoryPlatformRepository;
import com.beizhi.smartwash.interfaces.rest.error.BadRequestException;
import com.beizhi.smartwash.security.PermissionGuard;
import com.beizhi.smartwash.security.RequestContext;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportService {
  private static final List<String> VIEW_SCOPES = List.of(
    "PLATFORM_ADMIN",
    "PLATFORM_FINANCE",
    "AGENT_ADMIN",
    "STORE_ADMIN",
    "DEVICE_INVESTOR",
    "PROPERTY_ADMIN"
  );

  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public ReportService(
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

  public Map<String, Object> getSummary(Map<String, Object> query) {
    permissionGuard.check("report.read");
    RequestContext context = RequestContextHolder.get();

    String viewScope = normalizeViewScope(query.get("viewScope"), query.get("view"));
    String period = normalizePeriod(query.get("period"));
    OffsetDateTime now = OffsetDateTime.now();

    Map<String, Object> metrics = new LinkedHashMap<>();
    metrics.put("orderCount", repository.countAppOrders(context.tenantId()));
    metrics.put("cabinetCount", repository.countCabinets(context.tenantId()));
    metrics.put("investorCount", repository.countDeviceInvestors(context.tenantId()));
    metrics.put("ticketCount", repository.countTickets(context.tenantId()));
    metrics.put("refundCount", repository.countRefunds(context.tenantId()));
    metrics.put("settlementCount", repository.countSettlements(context.tenantId()));
    metrics.put("payoutAccountCount", repository.countPayoutAccounts(context.tenantId()));
    metrics.put("onTimeRate", 0.96d);
    metrics.put("grossAmount", 26800.0d);
    metrics.put("netAmount", 25260.0d);

    ReportSnapshot snapshot = new ReportSnapshot(
      UUID.randomUUID().toString(),
      context.tenantId(),
      viewScope,
      period,
      now,
      Map.copyOf(metrics)
    );
    repository.saveReportSnapshot(
      context.tenantId(),
      viewScope,
      Map.of(
        "snapshotId", snapshot.snapshotId(),
        "tenantId", snapshot.tenantId(),
        "viewScope", snapshot.viewScope(),
        "period", snapshot.period(),
        "generatedAt", snapshot.generatedAt().toString(),
        "metrics", snapshot.metrics()
      )
    );

    return Map.of(
      "snapshotId", snapshot.snapshotId(),
      "tenantId", snapshot.tenantId(),
      "viewScope", snapshot.viewScope(),
      "period", snapshot.period(),
      "generatedAt", snapshot.generatedAt().toString(),
      "metrics", snapshot.metrics(),
      "kpiCards", buildKpiCards(snapshot.metrics()),
      "trend", buildTrend()
    );
  }

  public Map<String, Object> exportReport(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("report.export");
    RequestContext context = RequestContextHolder.get();

    String viewScope = normalizeViewScope(body.get("viewScope"), body.get("view"));
    String period = normalizePeriod(body.get("period"));
    String format = normalizeFormat(body.get("format"));
    String requestHash = context.tenantId() + "|" + viewScope + "|" + period + "|" + format + "|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> exported = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "report-export:" + viewScope,
      idempotencyKey,
      requestHash,
      () -> doExport(context, body, viewScope, period, format)
    );
    return exported;
  }

  private Map<String, Object> doExport(
    RequestContext context,
    Map<String, Object> body,
    String viewScope,
    String period,
    String format
  ) {
    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime expiresAt = now.plusMinutes(15);
    String exportId = UUID.randomUUID().toString();
    String downloadUrl = "/exports/reports/" + exportId + "." + format.toLowerCase(Locale.ROOT) + "?exp=" + expiresAt.toEpochSecond();

    ReportExportJob job = new ReportExportJob(
      exportId,
      context.tenantId(),
      viewScope,
      "READY",
      format,
      downloadUrl,
      context.requestId(),
      now,
      body
    );
    repository.saveReportExport(
      context.tenantId(),
      Map.of(
        "exportId", job.exportId(),
        "tenantId", job.tenantId(),
        "viewScope", job.viewScope(),
        "status", job.status(),
        "format", job.format(),
        "downloadUrl", job.downloadUrl(),
        "requestId", job.requestId(),
        "createdAt", job.createdAt().toString(),
        "filters", job.filters()
      )
    );

    auditService.append(
      "REPORT_EXPORT_CREATE",
      "report-export:" + exportId,
      "SUCCESS",
      Map.of(
        "viewScope", viewScope,
        "period", period,
        "format", format
      )
    );

    return Map.of(
      "exportId", exportId,
      "tenantId", context.tenantId(),
      "viewScope", viewScope,
      "period", period,
      "format", format,
      "status", "READY",
      "downloadUrl", downloadUrl,
      "expiresAt", expiresAt.toString(),
      "requestId", context.requestId()
    );
  }

  private List<Map<String, Object>> buildKpiCards(Map<String, Object> metrics) {
    return List.of(
      Map.of("key", "orderCount", "label", "订单量", "value", metrics.get("orderCount"), "trend", "+12.8%"),
      Map.of("key", "grossAmount", "label", "交易额", "value", metrics.get("grossAmount"), "trend", "+8.4%"),
      Map.of("key", "netAmount", "label", "净入账", "value", metrics.get("netAmount"), "trend", "+7.1%"),
      Map.of("key", "onTimeRate", "label", "准时率", "value", metrics.get("onTimeRate"), "trend", "+1.2pt")
    );
  }

  private List<Map<String, Object>> buildTrend() {
    return List.of(
      Map.of("date", "2026-02-24", "orders", 112, "amount", 11800.0d),
      Map.of("date", "2026-02-25", "orders", 126, "amount", 12680.0d),
      Map.of("date", "2026-02-26", "orders", 119, "amount", 12250.0d),
      Map.of("date", "2026-02-27", "orders", 133, "amount", 13110.0d),
      Map.of("date", "2026-02-28", "orders", 128, "amount", 12790.0d)
    );
  }

  private String normalizeViewScope(Object primary, Object fallback) {
    String raw = asString(primary).trim();
    if (!StringUtils.hasText(raw)) {
      raw = asString(fallback).trim();
    }
    if (!StringUtils.hasText(raw)) {
      return "PLATFORM_ADMIN";
    }
    String normalized = raw.toUpperCase(Locale.ROOT);
    if (!VIEW_SCOPES.contains(normalized)) {
      throw new BadRequestException("Unsupported report viewScope: " + raw);
    }
    return normalized;
  }

  private String normalizePeriod(Object raw) {
    String period = asString(raw).trim();
    return StringUtils.hasText(period) ? period : "LAST_30_DAYS";
  }

  private String normalizeFormat(Object raw) {
    String format = asString(raw).trim();
    if (!StringUtils.hasText(format)) {
      return "CSV";
    }
    String normalized = format.toUpperCase(Locale.ROOT);
    if (!List.of("CSV", "XLSX", "PDF").contains(normalized)) {
      throw new BadRequestException("Unsupported export format: " + format);
    }
    return normalized;
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
