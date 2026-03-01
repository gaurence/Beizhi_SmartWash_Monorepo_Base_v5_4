package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.AbacPolicy;
import com.beizhi.smartwash.domain.model.AccessGrant;
import com.beizhi.smartwash.infrastructure.repository.InMemoryPlatformRepository;
import com.beizhi.smartwash.interfaces.rest.error.BadRequestException;
import com.beizhi.smartwash.interfaces.rest.error.NotFoundException;
import com.beizhi.smartwash.security.PermissionGuard;
import com.beizhi.smartwash.security.RequestContext;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AbacService {
  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public AbacService(
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

  public List<AbacPolicy> listPolicies(int page, int size) {
    permissionGuard.check("iam.abac.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.listAbacPolicies(tenantId, page, size);
  }

  public long countPolicies() {
    return repository.countAbacPolicies(RequestContextHolder.get().tenantId());
  }

  public AbacPolicy createPolicy(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("iam.abac.policy.manage");
    RequestContext context = RequestContextHolder.get();

    String policyCode = asString(body.get("policyCode")).trim().toUpperCase();
    String name = asString(body.get("name")).trim();
    String action = asString(body.get("action")).trim().toUpperCase();
    String resourceType = asString(body.get("resourceType")).trim().toUpperCase();
    String conditionExpr = asString(body.get("conditionExpr")).trim();

    if (!StringUtils.hasText(policyCode) || !StringUtils.hasText(name) || !StringUtils.hasText(action)) {
      throw new BadRequestException("policyCode, name and action are required");
    }

    String requestHash = context.tenantId() + "|" + policyCode + "|" + action + "|" + conditionExpr;
    final String finalResourceType = StringUtils.hasText(resourceType) ? resourceType : "ORDER_TASK";
    return (AbacPolicy) idempotencyService.execute(
      context.tenantId(),
      "abac-policy-create",
      idempotencyKey,
      requestHash,
      () -> doCreatePolicy(context.tenantId(), policyCode, name, action, finalResourceType, conditionExpr)
    );
  }

  public AbacPolicy updatePolicy(String policyId, Map<String, Object> body) {
    permissionGuard.check("iam.abac.policy.manage");
    RequestContext context = RequestContextHolder.get();
    AbacPolicy existing = repository.findAbacPolicy(context.tenantId(), policyId)
      .orElseThrow(() -> new NotFoundException("ABAC policy not found"));

    String name = asString(body.get("name")).trim();
    String action = asString(body.get("action")).trim().toUpperCase();
    String resourceType = asString(body.get("resourceType")).trim().toUpperCase();
    String conditionExpr = asString(body.get("conditionExpr")).trim();
    String enabledRaw = asString(body.get("enabled")).trim();

    boolean enabled = existing.enabled();
    if (StringUtils.hasText(enabledRaw)) {
      enabled = Boolean.parseBoolean(enabledRaw);
    }

    AbacPolicy updated = new AbacPolicy(
      existing.id(),
      existing.tenantId(),
      existing.policyCode(),
      StringUtils.hasText(name) ? name : existing.name(),
      StringUtils.hasText(action) ? action : existing.action(),
      StringUtils.hasText(resourceType) ? resourceType : existing.resourceType(),
      StringUtils.hasText(conditionExpr) ? conditionExpr : existing.conditionExpr(),
      enabled,
      existing.createdAt(),
      OffsetDateTime.now()
    );
    repository.saveAbacPolicy(updated);

    auditService.append(
      "ABAC_POLICY_UPDATE",
      "abac-policy:" + policyId,
      "SUCCESS",
      Map.of("policyCode", updated.policyCode(), "enabled", updated.enabled())
    );
    return updated;
  }

  public List<AccessGrant> listAccessGrants(int page, int size) {
    permissionGuard.check("iam.abac.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.listAccessGrants(tenantId, page, size);
  }

  public long countAccessGrants() {
    return repository.countAccessGrants(RequestContextHolder.get().tenantId());
  }

  public AccessGrant issueAccessGrant(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("iam.access-grant.issue");
    RequestContext context = RequestContextHolder.get();

    String userId = asString(body.get("userId")).trim();
    String action = asString(body.get("action")).trim().toUpperCase();
    String orderId = asString(body.get("orderId")).trim();
    String taskId = asString(body.get("taskId")).trim();
    String cabinetId = asString(body.get("cabinetId")).trim();
    String slotId = asString(body.get("slotId")).trim();
    int ttlSeconds = parseInt(body.get("ttlSeconds"), 300);
    String grantType = asString(body.get("grantType")).trim().toUpperCase();

    if (!StringUtils.hasText(userId)) {
      throw new BadRequestException("userId is required");
    }
    if (!StringUtils.hasText(action)) {
      throw new BadRequestException("action is required");
    }
    if (!StringUtils.hasText(orderId) && !StringUtils.hasText(taskId)) {
      throw new BadRequestException("orderId or taskId is required");
    }
    if (!StringUtils.hasText(cabinetId) || !StringUtils.hasText(slotId)) {
      throw new BadRequestException("cabinetId and slotId are required");
    }
    if (ttlSeconds < 30 || ttlSeconds > 600) {
      throw new BadRequestException("ttlSeconds must be between 30 and 600");
    }
    if (repository.findUser(context.tenantId(), userId).isEmpty()) {
      throw new BadRequestException("userId not found in tenant scope");
    }
    if (!StringUtils.hasText(grantType)) {
      grantType = "ORDER_SCOPED";
    }

    String requestHash = context.tenantId() + "|" + userId + "|" + action + "|" + orderId + "|" + taskId + "|" + cabinetId + "|" + slotId + "|" + ttlSeconds;
    final String finalGrantType = grantType;
    final int finalTtlSeconds = ttlSeconds;
    return (AccessGrant) idempotencyService.execute(
      context.tenantId(),
      "abac-grant-issue",
      idempotencyKey,
      requestHash,
      () -> doIssueGrant(context, userId, finalGrantType, action, orderId, taskId, cabinetId, slotId, finalTtlSeconds)
    );
  }

  private AbacPolicy doCreatePolicy(
    String tenantId,
    String policyCode,
    String name,
    String action,
    String resourceType,
    String conditionExpr
  ) {
    boolean exists = repository.listAbacPolicies(tenantId, 1, Integer.MAX_VALUE).stream()
      .anyMatch(policy -> policy.policyCode().equalsIgnoreCase(policyCode));
    if (exists) {
      throw new BadRequestException("policyCode already exists in tenant");
    }

    OffsetDateTime now = OffsetDateTime.now();
    AbacPolicy policy = new AbacPolicy(
      UUID.randomUUID().toString(),
      tenantId,
      policyCode,
      name,
      action,
      resourceType,
      conditionExpr,
      true,
      now,
      now
    );
    repository.saveAbacPolicy(policy);
    auditService.append(
      "ABAC_POLICY_CREATE",
      "abac-policy:" + policy.id(),
      "SUCCESS",
      Map.of("policyCode", policyCode, "action", action, "resourceType", resourceType)
    );
    return policy;
  }

  private AccessGrant doIssueGrant(
    RequestContext context,
    String userId,
    String grantType,
    String action,
    String orderId,
    String taskId,
    String cabinetId,
    String slotId,
    int ttlSeconds
  ) {
    OffsetDateTime now = OffsetDateTime.now();
    String grantId = UUID.randomUUID().toString();
    AccessGrant grant = new AccessGrant(
      grantId,
      context.tenantId(),
      userId,
      grantType,
      action,
      orderId,
      taskId,
      cabinetId,
      slotId,
      "grant:" + context.tenantId() + ":" + grantId + ":" + UUID.randomUUID(),
      now,
      now.plusSeconds(ttlSeconds),
      "ISSUED"
    );
    repository.saveAccessGrant(grant);

    auditService.append(
      "ABAC_ACCESS_GRANT_ISSUE",
      "access-grant:" + grant.id(),
      "SUCCESS",
      Map.of(
        "grantType", grantType,
        "action", action,
        "userId", userId,
        "orderId", orderId,
        "taskId", taskId,
        "cabinetId", cabinetId,
        "slotId", slotId,
        "ttlSeconds", ttlSeconds
      )
    );
    return grant;
  }

  private int parseInt(Object value, int defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(String.valueOf(value).trim());
    } catch (NumberFormatException ex) {
      throw new BadRequestException("invalid integer value: " + value);
    }
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
