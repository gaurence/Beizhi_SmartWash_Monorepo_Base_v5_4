package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.interfaces.rest.error.BadRequestException;
import com.beizhi.smartwash.interfaces.rest.error.NotFoundException;
import com.beizhi.smartwash.security.PermissionGuard;
import com.beizhi.smartwash.security.RequestContext;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RiskService {
  private static final Set<String> ALLOWED_POLICY_STATUS = Set.of("ACTIVE", "DISABLED");
  private static final int DEFAULT_PAGE_SIZE = 20;

  private final PermissionGuard permissionGuard;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;
  private final Map<String, Map<String, RiskPolicyState>> policiesByTenant = new ConcurrentHashMap<>();

  public RiskService(
    PermissionGuard permissionGuard,
    IdempotencyService idempotencyService,
    AuditService auditService
  ) {
    this.permissionGuard = permissionGuard;
    this.idempotencyService = idempotencyService;
    this.auditService = auditService;
  }

  public List<RiskPolicyView> listPolicies(int page, int size) {
    permissionGuard.check("risk.policy.read");
    String tenantId = RequestContextHolder.get().tenantId();
    List<RiskPolicyView> all = new ArrayList<>(policiesForTenant(tenantId).values()).stream()
      .sorted(Comparator.comparing(RiskPolicyState::updatedAt).reversed())
      .map(RiskPolicyState::toView)
      .toList();
    return slice(all, page, size);
  }

  public long countPolicies() {
    return policiesForTenant(RequestContextHolder.get().tenantId()).size();
  }

  public RiskPolicyView updatePolicy(String policyId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("risk.policy.manage");
    RequestContext context = RequestContextHolder.get();
    String requestHash = context.tenantId() + "|" + policyId + "|" + String.valueOf(body);

    return (RiskPolicyView) idempotencyService.execute(
      context.tenantId(),
      "risk-policy-update:" + policyId,
      idempotencyKey,
      requestHash,
      () -> doUpdatePolicy(context, policyId, body)
    );
  }

  private RiskPolicyView doUpdatePolicy(RequestContext context, String policyId, Map<String, Object> body) {
    Map<String, RiskPolicyState> tenantPolicies = policiesForTenant(context.tenantId());
    RiskPolicyState existing = tenantPolicies.get(policyId);
    if (existing == null) {
      throw new NotFoundException("Risk policy not found");
    }

    RiskPolicyState updated = new RiskPolicyState(
      existing.policyId(),
      existing.tenantId(),
      existing.policyCode(),
      firstNonBlank(body.get("policyName"), existing.policyName()),
      resolvePolicyStatus(body.get("status"), existing.status()),
      parseInt(body.get("triggerScore"), existing.triggerScore(), "triggerScore", 0, 100),
      parseInt(body.get("reviewWindowMinutes"), existing.reviewWindowMinutes(), "reviewWindowMinutes", 1, 1440),
      firstNonBlank(body.get("description"), existing.description()),
      OffsetDateTime.now(),
      context.userId(),
      context.requestId()
    );
    tenantPolicies.put(policyId, updated);

    auditService.append(
      "RISK_POLICY_UPDATE",
      "risk-policy:" + policyId,
      "SUCCESS",
      Map.of(
        "policyId", updated.policyId(),
        "status", updated.status(),
        "triggerScore", updated.triggerScore(),
        "reviewWindowMinutes", updated.reviewWindowMinutes()
      )
    );
    return updated.toView();
  }

  private Map<String, RiskPolicyState> policiesForTenant(String tenantId) {
    return policiesByTenant.computeIfAbsent(tenantId, this::seedPoliciesForTenant);
  }

  private Map<String, RiskPolicyState> seedPoliciesForTenant(String tenantId) {
    OffsetDateTime now = OffsetDateTime.now();
    String policyIdPrimary = "RP-" + tenantId.toUpperCase() + "-ORDER-SPIKE";
    String policyIdSecondary = "RP-" + tenantId.toUpperCase() + "-ABNORMAL-REFUND";

    Map<String, RiskPolicyState> seeded = new ConcurrentHashMap<>();
    seeded.put(policyIdPrimary, new RiskPolicyState(
      policyIdPrimary,
      tenantId,
      "ORDER_SPIKE_PROTECTION",
      "订单激增防护",
      "ACTIVE",
      75,
      30,
      "订单峰值异常时进入人工复核",
      now.minusHours(4),
      "system",
      "seed-" + tenantId
    ));
    seeded.put(policyIdSecondary, new RiskPolicyState(
      policyIdSecondary,
      tenantId,
      "ABNORMAL_REFUND_PROTECTION",
      "异常退款拦截",
      "ACTIVE",
      70,
      60,
      "短时间高频退款触发复核",
      now.minusHours(2),
      "system",
      "seed-" + tenantId
    ));
    return seeded;
  }

  private String resolvePolicyStatus(Object value, String fallback) {
    String text = asString(value).toUpperCase();
    if (!StringUtils.hasText(text)) {
      return fallback;
    }
    if (!ALLOWED_POLICY_STATUS.contains(text)) {
      throw new BadRequestException("status must be one of: ACTIVE, DISABLED");
    }
    return text;
  }

  private int parseInt(Object value, int fallback, String fieldName, int min, int max) {
    String text = asString(value);
    if (!StringUtils.hasText(text)) {
      return fallback;
    }
    try {
      int parsed = Integer.parseInt(text);
      if (parsed < min || parsed > max) {
        throw new BadRequestException(fieldName + " must be between " + min + " and " + max);
      }
      return parsed;
    } catch (NumberFormatException ex) {
      throw new BadRequestException(fieldName + " must be a number");
    }
  }

  private String firstNonBlank(Object candidate, String fallback) {
    String text = asString(candidate);
    return StringUtils.hasText(text) ? text : fallback;
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value).trim();
  }

  private List<RiskPolicyView> slice(List<RiskPolicyView> all, int page, int size) {
    int safePage = Math.max(page, 1);
    int safeSize = size > 0 ? size : DEFAULT_PAGE_SIZE;
    int from = (safePage - 1) * safeSize;
    if (from >= all.size()) {
      return List.of();
    }
    int to = Math.min(all.size(), from + safeSize);
    return all.subList(from, to);
  }

  private record RiskPolicyState(
    String policyId,
    String tenantId,
    String policyCode,
    String policyName,
    String status,
    int triggerScore,
    int reviewWindowMinutes,
    String description,
    OffsetDateTime updatedAt,
    String updatedBy,
    String requestId
  ) {
    private RiskPolicyView toView() {
      return new RiskPolicyView(
        policyId,
        policyCode,
        policyName,
        status,
        triggerScore,
        reviewWindowMinutes,
        description,
        updatedBy,
        updatedAt,
        requestId
      );
    }
  }

  public record RiskPolicyView(
    String policyId,
    String policyCode,
    String policyName,
    String status,
    int triggerScore,
    int reviewWindowMinutes,
    String description,
    String updatedBy,
    OffsetDateTime updatedAt,
    String requestId
  ) {}
}
