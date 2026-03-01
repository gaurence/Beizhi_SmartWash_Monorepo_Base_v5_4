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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {
  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public OrderService(
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

  public List<Map<String, Object>> listOrders(int page, int size) {
    permissionGuard.check("order.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.listAppOrders(tenantId, page, size).stream()
      .map(this::toOrderView)
      .toList();
  }

  public long countOrders() {
    permissionGuard.check("order.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.countAppOrders(tenantId);
  }

  public Map<String, Object> getOrder(String orderId) {
    permissionGuard.check("order.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.findAppOrder(tenantId, orderId)
      .map(this::toOrderView)
      .orElseThrow(() -> new NotFoundException("Order not found"));
  }

  public Map<String, Object> createOrder(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("order.manage");
    RequestContext context = RequestContextHolder.get();
    String requestHash = context.tenantId() + "|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> created = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "order-create",
      idempotencyKey,
      requestHash,
      () -> doCreateOrder(context, body)
    );
    return toOrderView(created);
  }

  public Map<String, Object> cancelOrder(String orderId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("order.manage");
    RequestContext context = RequestContextHolder.get();
    String requestHash = context.tenantId() + "|" + orderId + "|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> cancelled = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "order-cancel:" + orderId,
      idempotencyKey,
      requestHash,
      () -> doCancelOrder(context, orderId, body)
    );
    return toOrderView(cancelled);
  }

  public Map<String, Object> dispatchOrder(String orderId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("order.manage");
    RequestContext context = RequestContextHolder.get();
    String requestHash = context.tenantId() + "|" + orderId + "|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> dispatched = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "order-dispatch:" + orderId,
      idempotencyKey,
      requestHash,
      () -> doDispatchOrder(context, orderId, body)
    );
    return toOrderView(dispatched);
  }

  public List<Map<String, Object>> getOrderTimeline(String orderId) {
    permissionGuard.check("order.read");
    String tenantId = RequestContextHolder.get().tenantId();
    repository.findAppOrder(tenantId, orderId).orElseThrow(() -> new NotFoundException("Order not found"));
    return repository.listAppOrderTimeline(tenantId, orderId);
  }

  public List<Map<String, Object>> getOrderEvidence(String orderId) {
    permissionGuard.check("order.read");
    String tenantId = RequestContextHolder.get().tenantId();
    repository.findAppOrder(tenantId, orderId).orElseThrow(() -> new NotFoundException("Order not found"));
    return repository.listAppOrderEvidence(tenantId, orderId).stream()
      .map(this::toEvidenceView)
      .toList();
  }

  public Map<String, Object> issueEvidenceUploadUrl(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("order.manage");
    RequestContext context = RequestContextHolder.get();
    String orderId = asString(body.get("orderId"));
    if (!StringUtils.hasText(orderId)) {
      throw new BadRequestException("orderId is required");
    }
    repository.findAppOrder(context.tenantId(), orderId).orElseThrow(() -> new NotFoundException("Order not found"));

    String stage = asString(body.get("stage"));
    String mediaType = asString(body.get("mediaType"));
    String fileName = asString(body.get("fileName"));
    String requestHash = context.tenantId() + "|" + orderId + "|" + stage + "|" + mediaType + "|" + fileName + "|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> uploadInfo = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "order-evidence-upload-url:" + orderId,
      idempotencyKey,
      requestHash,
      () -> doIssueEvidenceUploadUrl(context, orderId, stage, mediaType, fileName)
    );
    return uploadInfo;
  }

  public Map<String, Object> appendEvidenceRecord(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("order.manage");
    RequestContext context = RequestContextHolder.get();
    String orderId = asString(body.get("orderId"));
    if (!StringUtils.hasText(orderId)) {
      throw new BadRequestException("orderId is required");
    }
    repository.findAppOrder(context.tenantId(), orderId).orElseThrow(() -> new NotFoundException("Order not found"));

    String objectKey = asString(body.get("objectKey"));
    String sha256 = asString(body.get("sha256"));
    String stage = asString(body.get("stage"));
    String mediaType = asString(body.get("mediaType"));
    if (!StringUtils.hasText(objectKey) || !StringUtils.hasText(sha256) || !StringUtils.hasText(stage) || !StringUtils.hasText(mediaType)) {
      throw new BadRequestException("objectKey, sha256, stage and mediaType are required");
    }

    String requestHash = context.tenantId() + "|" + orderId + "|" + objectKey + "|" + sha256 + "|" + stage + "|" + mediaType;
    @SuppressWarnings("unchecked")
    Map<String, Object> evidence = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "order-evidence-record:" + orderId,
      idempotencyKey,
      requestHash,
      () -> doAppendEvidenceRecord(context, orderId, objectKey, sha256, stage, mediaType, body)
    );
    return evidence;
  }

  private Map<String, Object> doCreateOrder(RequestContext context, Map<String, Object> body) {
    OffsetDateTime now = OffsetDateTime.now();
    String orderId = asString(body.get("orderId"));
    if (!StringUtils.hasText(orderId)) {
      orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    String servicePackageId = asString(body.get("servicePackageId"));
    if (!StringUtils.hasText(servicePackageId)) {
      servicePackageId = "STANDARD_CARE";
    }

    String servicePackageName = asString(body.get("servicePackageName"));
    if (!StringUtils.hasText(servicePackageName)) {
      servicePackageName = "Standard Care";
    }

    String customerId = asString(body.get("customerId"));
    if (!StringUtils.hasText(customerId)) {
      customerId = "customer-0000";
    }

    Map<String, Object> order = new LinkedHashMap<>();
    order.put("orderId", orderId);
    order.put("tenantId", context.tenantId());
    order.put("customerId", customerId);
    order.put("status", "CREATED");
    order.put("fulfillmentMode", "ONSITE");
    order.put("servicePackageId", servicePackageId);
    order.put("servicePackageName", servicePackageName);
    order.put("addonIds", normalizeStringList(body.get("addonIds")));
    order.put("couponCode", asString(body.get("couponCode")));
    order.put("currency", "CNY");
    order.put("amountSubtotal", toAmount(body.get("amountSubtotal"), 99d));
    order.put("amountDiscount", toAmount(body.get("amountDiscount"), 0d));
    order.put("amountTotal", toAmount(body.get("amountTotal"), 99d));
    order.put("createdAt", now.toString());
    order.put("updatedAt", now.toString());
    order.put("requestId", context.requestId());

    repository.saveAppOrder(context.tenantId(), order);
    repository.appendAppOrderTimeline(
      context.tenantId(),
      orderId,
      "ORDER_CREATED",
      "Order Created",
      "Order created by store admin"
    );

    auditService.append(
      "ORDER_CREATE",
      "order:" + orderId,
      "SUCCESS",
      Map.of(
        "orderId", orderId,
        "servicePackageId", servicePackageId
      )
    );
    return order;
  }

  private Map<String, Object> doCancelOrder(RequestContext context, String orderId, Map<String, Object> body) {
    Map<String, Object> order = repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));

    String currentStatus = asString(order.get("status")).toUpperCase(Locale.ROOT);
    if ("CANCELLED".equals(currentStatus)) {
      return order;
    }
    if ("COMPLETED".equals(currentStatus) || "CLOSED".equals(currentStatus)) {
      throw new BadRequestException("Order cannot be cancelled in status: " + currentStatus);
    }

    OffsetDateTime now = OffsetDateTime.now();
    String reason = asString(body.get("reason"));
    if (!StringUtils.hasText(reason)) {
      reason = "STORE_ADMIN_REQUESTED";
    }

    Map<String, Object> updated = new LinkedHashMap<>(order);
    updated.put("status", "CANCELLED");
    updated.put("cancelReason", reason);
    updated.put("cancelledAt", now.toString());
    updated.put("updatedAt", now.toString());
    updated.put("requestId", context.requestId());

    repository.saveAppOrder(context.tenantId(), updated);
    repository.appendAppOrderTimeline(
      context.tenantId(),
      orderId,
      "ORDER_CANCELLED",
      "Order Cancelled",
      "Order cancelled by store admin"
    );
    auditService.append(
      "ORDER_CANCEL",
      "order:" + orderId,
      "SUCCESS",
      Map.of(
        "orderId", orderId,
        "fromStatus", currentStatus,
        "reason", reason
      )
    );
    return updated;
  }

  private Map<String, Object> doDispatchOrder(RequestContext context, String orderId, Map<String, Object> body) {
    Map<String, Object> order = repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));

    String currentStatus = asString(order.get("status")).toUpperCase(Locale.ROOT);
    if ("DISPATCHED".equals(currentStatus)) {
      return order;
    }
    if (!"CREATED".equals(currentStatus)) {
      throw new BadRequestException("Order cannot be dispatched in status: " + currentStatus);
    }

    OffsetDateTime now = OffsetDateTime.now();
    String assigneeUserId = asString(body.get("assigneeUserId"));
    if (!StringUtils.hasText(assigneeUserId)) {
      assigneeUserId = "tech01";
    }

    Map<String, Object> updated = new LinkedHashMap<>(order);
    updated.put("status", "DISPATCHED");
    updated.put("dispatchAssigneeUserId", assigneeUserId);
    updated.put("dispatchedAt", now.toString());
    updated.put("updatedAt", now.toString());
    updated.put("requestId", context.requestId());

    repository.saveAppOrder(context.tenantId(), updated);
    repository.appendAppOrderTimeline(
      context.tenantId(),
      orderId,
      "ORDER_DISPATCHED",
      "Order Dispatched",
      "Order assigned to " + assigneeUserId
    );
    auditService.append(
      "ORDER_DISPATCH",
      "order:" + orderId,
      "SUCCESS",
      Map.of(
        "orderId", orderId,
        "fromStatus", currentStatus,
        "assigneeUserId", assigneeUserId
      )
    );
    return updated;
  }

  private Map<String, Object> toOrderView(Map<String, Object> order) {
    Map<String, Object> view = new LinkedHashMap<>();
    view.put("orderId", asString(order.get("orderId")));
    view.put("customerId", asString(order.get("customerId")));
    view.put("status", asString(order.get("status")));
    view.put("fulfillmentMode", asString(order.get("fulfillmentMode")));
    view.put("servicePackageId", asString(order.get("servicePackageId")));
    view.put("servicePackageName", asString(order.get("servicePackageName")));
    view.put("addonIds", normalizeStringList(order.get("addonIds")));
    view.put("couponCode", asString(order.get("couponCode")));
    view.put("currency", asString(order.get("currency")));
    view.put("amountSubtotal", toAmount(order.get("amountSubtotal"), 0d));
    view.put("amountDiscount", toAmount(order.get("amountDiscount"), 0d));
    view.put("amountTotal", toAmount(order.get("amountTotal"), 0d));
    view.put("dispatchAssigneeUserId", asString(order.get("dispatchAssigneeUserId")));
    view.put("createdAt", asString(order.get("createdAt")));
    view.put("updatedAt", asString(order.get("updatedAt")));
    view.put("dispatchedAt", asString(order.get("dispatchedAt")));
    view.put("cancelledAt", asString(order.get("cancelledAt")));
    view.put("requestId", asString(order.get("requestId")));
    return view;
  }

  private Map<String, Object> doIssueEvidenceUploadUrl(
    RequestContext context,
    String orderId,
    String stage,
    String mediaType,
    String fileName
  ) {
    String normalizedStage = StringUtils.hasText(stage) ? stage.trim().toUpperCase(Locale.ROOT) : "SERVICE";
    String normalizedMediaType = StringUtils.hasText(mediaType) ? mediaType.trim().toUpperCase(Locale.ROOT) : "IMAGE";
    String normalizedFileName = StringUtils.hasText(fileName) ? fileName.trim() : "evidence.jpg";
    OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(10);
    String objectKey = "evidence/" + context.tenantId() + "/" + orderId + "/" + UUID.randomUUID() + "-" + normalizedFileName;
    String uploadUrl = "/storage/upload/" + objectKey + "?exp=" + expiresAt.toEpochSecond();

    auditService.append(
      "ORDER_EVIDENCE_UPLOAD_URL_CREATE",
      "order:" + orderId,
      "SUCCESS",
      Map.of(
        "orderId", orderId,
        "stage", normalizedStage,
        "mediaType", normalizedMediaType,
        "objectKey", objectKey
      )
    );

    return Map.of(
      "orderId", orderId,
      "objectKey", objectKey,
      "uploadUrl", uploadUrl,
      "expiresAt", expiresAt.toString(),
      "stage", normalizedStage,
      "mediaType", normalizedMediaType,
      "requestId", context.requestId()
    );
  }

  private Map<String, Object> doAppendEvidenceRecord(
    RequestContext context,
    String orderId,
    String objectKey,
    String sha256,
    String stage,
    String mediaType,
    Map<String, Object> body
  ) {
    Map<String, Object> evidence = new LinkedHashMap<>();
    evidence.put("evidenceId", asString(body.get("evidenceId")));
    evidence.put("orderId", orderId);
    evidence.put("stage", stage.trim().toUpperCase(Locale.ROOT));
    evidence.put("mediaType", mediaType.trim().toUpperCase(Locale.ROOT));
    evidence.put("objectKey", objectKey);
    evidence.put("sha256", sha256);
    evidence.put("uploaderUserId", context.userId());
    evidence.put("uploaderRole", asString(body.get("uploaderRole")).isBlank() ? "OPERATOR" : asString(body.get("uploaderRole")));
    evidence.put("capturedAt", asString(body.get("capturedAt")));
    evidence.put("requestId", context.requestId());

    repository.appendAppOrderEvidence(context.tenantId(), orderId, evidence);
    auditService.append(
      "ORDER_EVIDENCE_RECORD_APPEND",
      "order-evidence:" + orderId,
      "SUCCESS",
      Map.of(
        "orderId", orderId,
        "stage", evidence.get("stage"),
        "objectKey", objectKey
      )
    );

    return toEvidenceView(evidence);
  }

  private Map<String, Object> toEvidenceView(Map<String, Object> evidence) {
    Map<String, Object> view = new LinkedHashMap<>();
    view.put("evidenceId", asString(evidence.get("evidenceId")));
    view.put("orderId", asString(evidence.get("orderId")));
    view.put("stage", asString(evidence.get("stage")));
    view.put("mediaType", asString(evidence.get("mediaType")));
    view.put("objectKey", asString(evidence.get("objectKey")));
    view.put("sha256", asString(evidence.get("sha256")));
    view.put("uploaderUserId", asString(evidence.get("uploaderUserId")));
    view.put("uploaderRole", asString(evidence.get("uploaderRole")));
    view.put("capturedAt", asString(evidence.get("capturedAt")));
    view.put("createdAt", asString(evidence.get("createdAt")));
    view.put("requestId", asString(evidence.get("requestId")));
    return view;
  }

  private List<String> normalizeStringList(Object value) {
    if (!(value instanceof List<?> raw)) {
      return List.of();
    }
    return raw.stream().map(String::valueOf).toList();
  }

  private double toAmount(Object value, double defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    String text = asString(value);
    if (!StringUtils.hasText(text)) {
      return defaultValue;
    }
    try {
      return Double.parseDouble(text);
    } catch (NumberFormatException ex) {
      throw new BadRequestException("Invalid amount: " + text);
    }
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value).trim();
  }
}
