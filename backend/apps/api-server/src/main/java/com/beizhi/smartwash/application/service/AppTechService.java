package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.Slot;
import com.beizhi.smartwash.domain.model.SlotState;
import com.beizhi.smartwash.domain.service.SlotStateMachine;
import com.beizhi.smartwash.infrastructure.repository.InMemoryPlatformRepository;
import com.beizhi.smartwash.interfaces.rest.error.BadRequestException;
import com.beizhi.smartwash.interfaces.rest.error.ConflictException;
import com.beizhi.smartwash.interfaces.rest.error.ForbiddenException;
import com.beizhi.smartwash.interfaces.rest.error.NotFoundException;
import com.beizhi.smartwash.security.PermissionGuard;
import com.beizhi.smartwash.security.RequestContext;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.UUID;

@Service
public class AppTechService {
  private static final Set<String> SUPPORTED_ROLE_MODES = Set.of("TECHNICIAN", "VALET");
  private static final Pattern HH_MM_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final SlotStateMachine slotStateMachine;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;
  private final ConcurrentMap<String, String> roleModeByTenantUser = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Map<String, Object>> notificationPrefsByTenantUser = new ConcurrentHashMap<>();

  public AppTechService(
    InMemoryPlatformRepository repository,
    PermissionGuard permissionGuard,
    SlotStateMachine slotStateMachine,
    IdempotencyService idempotencyService,
    AuditService auditService
  ) {
    this.repository = repository;
    this.permissionGuard = permissionGuard;
    this.slotStateMachine = slotStateMachine;
    this.idempotencyService = idempotencyService;
    this.auditService = auditService;
  }

  public Map<String, Object> getProfile() {
    permissionGuard.check("app.tech.profile.read");
    RequestContext context = RequestContextHolder.get();
    String roleMode = currentRoleMode(context.tenantId(), context.userId());
    return toProfileView(context, roleMode, false, "");
  }

  public Map<String, Object> getNotificationPrefs() {
    permissionGuard.check("app.tech.notifications.read");
    RequestContext context = RequestContextHolder.get();
    return resolveNotificationPrefs(context);
  }

  public Map<String, Object> updateNotificationPrefs(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("app.tech.notifications.update");
    RequestContext context = RequestContextHolder.get();
    String requestHash = context.tenantId()
      + "|" + context.userId()
      + "|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> response = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "app-tech-notification-prefs-update:" + context.userId(),
      idempotencyKey,
      requestHash,
      () -> doUpdateNotificationPrefs(context, body)
    );
    return response;
  }

  public Map<String, Object> switchRoleMode(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("app.tech.role-mode.switch");
    RequestContext context = RequestContextHolder.get();
    String targetRoleMode = normalizeRequiredRoleMode(body);
    String requestHash = context.tenantId()
      + "|" + context.userId()
      + "|" + targetRoleMode
      + "|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> response = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "app-tech-role-mode-switch:" + context.userId(),
      idempotencyKey,
      requestHash,
      () -> doSwitchRoleMode(context, targetRoleMode)
    );
    return response;
  }

  public List<Map<String, Object>> listTasks(int page, int size, String status) {
    permissionGuard.check("app.task.read");
    RequestContext context = RequestContextHolder.get();
    String normalizedStatus = normalizeStatus(status);
    return repository.listAppTechTasks(context.tenantId(), context.userId(), normalizedStatus, page, size)
      .stream()
      .map(this::toTaskView)
      .toList();
  }

  public long countTasks(String status) {
    permissionGuard.check("app.task.read");
    RequestContext context = RequestContextHolder.get();
    return repository.countAppTechTasks(context.tenantId(), context.userId(), normalizeStatus(status));
  }

  public Map<String, Object> acceptTask(String taskId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("app.task.accept");
    RequestContext context = RequestContextHolder.get();
    String requestHash = context.tenantId() + "|" + taskId + "|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> accepted = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "app-tech-task-accept:" + taskId,
      idempotencyKey,
      requestHash,
      () -> doAccept(context, taskId)
    );
    return toTaskView(accepted);
  }

  public Map<String, Object> completeTask(String taskId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("app.task.complete");
    RequestContext context = RequestContextHolder.get();
    String requestHash = context.tenantId() + "|" + taskId + "|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> completed = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "app-tech-task-complete:" + taskId,
      idempotencyKey,
      requestHash,
      () -> doComplete(context, taskId)
    );
    return toTaskView(completed);
  }

  public Map<String, Object> issueEvidenceUploadUrl(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("app.tech.evidence.write");
    RequestContext context = RequestContextHolder.get();
    String orderId = asString(body.get("orderId"));
    if (orderId.isBlank()) {
      throw new BadRequestException("orderId is required");
    }
    Map<String, Object> order = repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));
    ensureTechOrderScope(context, order);

    String stage = asString(body.get("stage"));
    String mediaType = asString(body.get("mediaType"));
    String fileName = asString(body.get("fileName"));
    String requestHash = context.tenantId() + "|" + orderId + "|" + stage + "|" + mediaType + "|" + fileName + "|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "app-tech-evidence-upload-url:" + orderId,
      idempotencyKey,
      requestHash,
      () -> doIssueEvidenceUploadUrl(context, orderId, stage, mediaType, fileName)
    );
    return result;
  }

  public Map<String, Object> appendEvidenceRecord(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("app.tech.evidence.write");
    RequestContext context = RequestContextHolder.get();
    String orderId = asString(body.get("orderId"));
    if (orderId.isBlank()) {
      throw new BadRequestException("orderId is required");
    }
    Map<String, Object> order = repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));
    ensureTechOrderScope(context, order);

    String objectKey = asString(body.get("objectKey"));
    String sha256 = asString(body.get("sha256"));
    String stage = asString(body.get("stage"));
    String mediaType = asString(body.get("mediaType"));
    if (objectKey.isBlank() || sha256.isBlank() || stage.isBlank() || mediaType.isBlank()) {
      throw new BadRequestException("objectKey, sha256, stage and mediaType are required");
    }

    String requestHash = context.tenantId() + "|" + orderId + "|" + objectKey + "|" + sha256 + "|" + stage + "|" + mediaType;
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "app-tech-evidence-record:" + orderId,
      idempotencyKey,
      requestHash,
      () -> doAppendEvidenceRecord(context, orderId, objectKey, sha256, stage, mediaType)
    );
    return result;
  }

  public Map<String, Object> pickupKey(String orderId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("app.tech.order.pickup-key");
    RequestContext context = RequestContextHolder.get();
    Map<String, Object> order = repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));
    ensureTechOrderScope(context, order);

    String cabinetId = resolveCabinetId(order, body);
    String slotNo = resolveSlotNo(order, body);
    String requestHash = context.tenantId() + "|" + orderId + "|" + cabinetId + "|" + slotNo + "|PICKUP_KEY";

    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "app-tech-pickup-key:" + orderId,
      idempotencyKey,
      requestHash,
      () -> repository.withSlotLock(
        context.tenantId(),
        cabinetId,
        slotNo,
        () -> doPickupKey(context, orderId, cabinetId, slotNo)
      )
    );
    return result;
  }

  public Map<String, Object> returnKey(String orderId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("app.tech.order.return-key");
    RequestContext context = RequestContextHolder.get();
    Map<String, Object> order = repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));
    ensureTechOrderScope(context, order);

    String cabinetId = resolveCabinetId(order, body);
    String slotNo = resolveSlotNo(order, body);
    String requestHash = context.tenantId() + "|" + orderId + "|" + cabinetId + "|" + slotNo + "|RETURN_KEY";

    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "app-tech-return-key:" + orderId,
      idempotencyKey,
      requestHash,
      () -> repository.withSlotLock(
        context.tenantId(),
        cabinetId,
        slotNo,
        () -> doReturnKey(context, orderId, cabinetId, slotNo)
      )
    );
    return result;
  }

  private Map<String, Object> doIssueEvidenceUploadUrl(
    RequestContext context,
    String orderId,
    String stage,
    String mediaType,
    String fileName
  ) {
    String normalizedStage = stage.isBlank() ? "SERVICE" : stage.trim().toUpperCase(Locale.ROOT);
    String normalizedMediaType = mediaType.isBlank() ? "IMAGE" : mediaType.trim().toUpperCase(Locale.ROOT);
    String normalizedFileName = fileName.isBlank() ? "tech-evidence.jpg" : fileName.trim();
    OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(10);
    String objectKey = "evidence/" + context.tenantId() + "/" + orderId + "/" + UUID.randomUUID() + "-" + normalizedFileName;
    String uploadUrl = "/storage/upload/" + objectKey + "?exp=" + expiresAt.toEpochSecond();

    auditService.append(
      "APP_TECH_EVIDENCE_UPLOAD_URL_CREATE",
      "app-order:" + orderId,
      "SUCCESS",
      Map.of("orderId", orderId, "stage", normalizedStage, "mediaType", normalizedMediaType, "objectKey", objectKey)
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
    String mediaType
  ) {
    Map<String, Object> evidence = new LinkedHashMap<>();
    evidence.put("orderId", orderId);
    evidence.put("stage", stage.trim().toUpperCase(Locale.ROOT));
    evidence.put("mediaType", mediaType.trim().toUpperCase(Locale.ROOT));
    evidence.put("objectKey", objectKey);
    evidence.put("sha256", sha256);
    evidence.put("uploaderUserId", context.userId());
    evidence.put("uploaderRole", "TECHNICIAN");
    evidence.put("capturedAt", OffsetDateTime.now().toString());
    evidence.put("requestId", context.requestId());
    repository.appendAppOrderEvidence(context.tenantId(), orderId, evidence);

    auditService.append(
      "APP_TECH_EVIDENCE_RECORD_APPEND",
      "app-order:" + orderId,
      "SUCCESS",
      Map.of("orderId", orderId, "stage", evidence.get("stage"), "objectKey", objectKey)
    );
    return Map.copyOf(evidence);
  }

  private Map<String, Object> doPickupKey(RequestContext context, String orderId, String cabinetId, String slotNo) {
    Map<String, Object> order = repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));
    Slot slot = repository.findSlot(context.tenantId(), cabinetId, slotNo)
      .orElseThrow(() -> new NotFoundException("Slot not found"));

    String orderStatus = asString(order.get("status")).toUpperCase(Locale.ROOT);
    if ("KEY_PICKED_UP".equals(orderStatus)
      && cabinetId.equals(asString(order.get("keyCabinetId")))
      && slotNo.equals(asString(order.get("keySlotNo")))) {
      return keyFlowResult(orderId, "KEY_PICKED_UP", cabinetId, slotNo, asString(order.get("keyPickedUpAt")));
    }

    SlotState fromState = slot.state();
    if (fromState != SlotState.OCCUPIED) {
      throw new ConflictException("Slot is not available for pickup in state: " + fromState);
    }

    slotStateMachine.assertTransitionAllowed(fromState, SlotState.RESERVED);
    slot.transitionTo(SlotState.RESERVED);
    repository.saveSlot(slot);

    OffsetDateTime now = OffsetDateTime.now();
    Map<String, Object> updatedOrder = new LinkedHashMap<>(order);
    updatedOrder.put("status", "KEY_PICKED_UP");
    updatedOrder.put("keyCabinetId", cabinetId);
    updatedOrder.put("keySlotNo", slotNo);
    updatedOrder.put("keyPickedUpAt", now.toString());
    updatedOrder.put("updatedAt", now.toString());
    updatedOrder.put("requestId", context.requestId());
    repository.saveAppOrder(context.tenantId(), updatedOrder);
    repository.appendAppOrderTimeline(
      context.tenantId(),
      orderId,
      "ORDER_KEY_PICKED_UP",
      "Key Picked Up",
      "Technician picked up key from cabinet slot"
    );
    repository.appendEvent(
      context.tenantId(),
      cabinetId,
      "ORDER_KEY_PICKED_UP",
      Map.of("orderId", orderId, "slotNo", slotNo, "requestId", context.requestId())
    );
    auditService.append(
      "APP_TECH_ORDER_PICKUP_KEY",
      "app-order:" + orderId,
      "SUCCESS",
      Map.of("orderId", orderId, "cabinetId", cabinetId, "slotNo", slotNo, "fromState", fromState.name())
    );
    return keyFlowResult(orderId, "KEY_PICKED_UP", cabinetId, slotNo, now.toString());
  }

  private Map<String, Object> doReturnKey(RequestContext context, String orderId, String cabinetId, String slotNo) {
    Map<String, Object> order = repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));
    Slot slot = repository.findSlot(context.tenantId(), cabinetId, slotNo)
      .orElseThrow(() -> new NotFoundException("Slot not found"));

    String orderStatus = asString(order.get("status")).toUpperCase(Locale.ROOT);
    if ("KEY_RETURNED".equals(orderStatus)
      && cabinetId.equals(asString(order.get("keyCabinetId")))
      && slotNo.equals(asString(order.get("keySlotNo")))) {
      return keyFlowResult(orderId, "KEY_RETURNED", cabinetId, slotNo, asString(order.get("keyReturnedAt")));
    }

    SlotState fromState = slot.state();
    if (fromState != SlotState.RESERVED) {
      throw new ConflictException("Slot is not available for return in state: " + fromState);
    }

    slotStateMachine.assertTransitionAllowed(fromState, SlotState.OCCUPIED);
    slot.transitionTo(SlotState.OCCUPIED);
    repository.saveSlot(slot);

    OffsetDateTime now = OffsetDateTime.now();
    Map<String, Object> updatedOrder = new LinkedHashMap<>(order);
    updatedOrder.put("status", "KEY_RETURNED");
    updatedOrder.put("keyCabinetId", cabinetId);
    updatedOrder.put("keySlotNo", slotNo);
    updatedOrder.put("keyReturnedAt", now.toString());
    updatedOrder.put("updatedAt", now.toString());
    updatedOrder.put("requestId", context.requestId());
    repository.saveAppOrder(context.tenantId(), updatedOrder);
    repository.appendAppOrderTimeline(
      context.tenantId(),
      orderId,
      "ORDER_KEY_RETURNED",
      "Key Returned",
      "Technician returned key to cabinet slot"
    );
    repository.appendEvent(
      context.tenantId(),
      cabinetId,
      "ORDER_KEY_RETURNED",
      Map.of("orderId", orderId, "slotNo", slotNo, "requestId", context.requestId())
    );
    auditService.append(
      "APP_TECH_ORDER_RETURN_KEY",
      "app-order:" + orderId,
      "SUCCESS",
      Map.of("orderId", orderId, "cabinetId", cabinetId, "slotNo", slotNo, "fromState", fromState.name())
    );
    return keyFlowResult(orderId, "KEY_RETURNED", cabinetId, slotNo, now.toString());
  }

  private Map<String, Object> keyFlowResult(String orderId, String status, String cabinetId, String slotNo, String changedAt) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("orderId", orderId);
    result.put("status", status);
    result.put("cabinetId", cabinetId);
    result.put("slotNo", slotNo);
    result.put("changedAt", changedAt);
    result.put("requestId", RequestContextHolder.get().requestId());
    return result;
  }

  private void ensureTechOrderScope(RequestContext context, Map<String, Object> order) {
    String assignee = asString(order.get("dispatchAssigneeUserId"));
    if (assignee.isBlank()) {
      return;
    }
    if (!assignee.equals(context.userId())) {
      throw new ForbiddenException("Order is not assigned to current technician");
    }
  }

  private String resolveCabinetId(Map<String, Object> order, Map<String, Object> body) {
    String cabinetId = asString(body.get("cabinetId"));
    if (cabinetId.isBlank()) {
      cabinetId = asString(order.get("keyCabinetId"));
    }
    if (cabinetId.isBlank()) {
      throw new BadRequestException("cabinetId is required");
    }
    return cabinetId;
  }

  private String resolveSlotNo(Map<String, Object> order, Map<String, Object> body) {
    String slotNo = asString(body.get("slotNo"));
    if (slotNo.isBlank()) {
      slotNo = asString(order.get("keySlotNo"));
    }
    if (slotNo.isBlank()) {
      throw new BadRequestException("slotNo is required");
    }
    return slotNo;
  }

  private Map<String, Object> doAccept(RequestContext context, String taskId) {
    Map<String, Object> task = repository.findAppTechTask(context.tenantId(), taskId)
      .orElseThrow(() -> new NotFoundException("Task not found"));

    ensureTaskAssignee(context.userId(), task);
    String fromStatus = asString(task.get("status")).toUpperCase(Locale.ROOT);

    if ("ACCEPTED".equals(fromStatus)) {
      return task;
    }
    if (!"DISPATCHED".equals(fromStatus)) {
      throw new BadRequestException("Task cannot be accepted in status: " + fromStatus);
    }

    OffsetDateTime now = OffsetDateTime.now();
    Map<String, Object> updated = new LinkedHashMap<>(task);
    updated.put("status", "ACCEPTED");
    updated.put("acceptedAt", now.toString());
    updated.put("updatedAt", now.toString());
    updated.put("requestId", context.requestId());

    repository.saveAppTechTask(context.tenantId(), updated);
    auditService.append(
      "APP_TECH_TASK_ACCEPT",
      "task:" + taskId,
      "SUCCESS",
      Map.of(
        "taskId", taskId,
        "orderId", asString(task.get("orderId")),
        "fromStatus", fromStatus,
        "toStatus", "ACCEPTED"
      )
    );
    return updated;
  }

  private Map<String, Object> doComplete(RequestContext context, String taskId) {
    Map<String, Object> task = repository.findAppTechTask(context.tenantId(), taskId)
      .orElseThrow(() -> new NotFoundException("Task not found"));

    ensureTaskAssignee(context.userId(), task);
    String fromStatus = asString(task.get("status")).toUpperCase(Locale.ROOT);

    if ("COMPLETED".equals(fromStatus)) {
      return task;
    }
    if (!"ACCEPTED".equals(fromStatus) && !"IN_PROGRESS".equals(fromStatus)) {
      throw new BadRequestException("Task cannot be completed in status: " + fromStatus);
    }

    OffsetDateTime now = OffsetDateTime.now();
    Map<String, Object> updated = new LinkedHashMap<>(task);
    updated.put("status", "COMPLETED");
    updated.put("completedAt", now.toString());
    updated.put("updatedAt", now.toString());
    updated.put("requestId", context.requestId());

    repository.saveAppTechTask(context.tenantId(), updated);
    auditService.append(
      "APP_TECH_TASK_COMPLETE",
      "task:" + taskId,
      "SUCCESS",
      Map.of(
        "taskId", taskId,
        "orderId", asString(task.get("orderId")),
        "fromStatus", fromStatus,
        "toStatus", "COMPLETED"
      )
    );
    return updated;
  }

  private void ensureTaskAssignee(String userId, Map<String, Object> task) {
    String assigneeUserId = asString(task.get("assigneeUserId"));
    if (!assigneeUserId.equals(userId)) {
      throw new ForbiddenException("Task is not assigned to current technician");
    }
  }

  private Map<String, Object> doSwitchRoleMode(RequestContext context, String targetRoleMode) {
    List<String> availableRoleModes = availableRoleModes(context.userId());
    if (!availableRoleModes.contains(targetRoleMode)) {
      throw new ForbiddenException("Current user cannot switch to role mode: " + targetRoleMode);
    }

    String currentRoleMode = currentRoleMode(context.tenantId(), context.userId());
    String switchedAt = "";
    if (!currentRoleMode.equals(targetRoleMode)) {
      roleModeByTenantUser.put(roleModeKey(context.tenantId(), context.userId()), targetRoleMode);
      switchedAt = OffsetDateTime.now().toString();
      auditService.append(
        "APP_TECH_ROLE_MODE_SWITCH",
        "app-tech-profile:" + context.userId(),
        "SUCCESS",
        Map.of(
          "userId", context.userId(),
          "fromRoleMode", currentRoleMode,
          "toRoleMode", targetRoleMode
        )
      );
    }
    return toProfileView(context, targetRoleMode, true, switchedAt);
  }

  private Map<String, Object> doUpdateNotificationPrefs(RequestContext context, Map<String, Object> body) {
    Map<String, Object> current = new LinkedHashMap<>(resolveNotificationPrefs(context));
    List<String> changedFields = new ArrayList<>();

    updateBooleanField(body, current, "inAppEnabled", changedFields);
    updateBooleanField(body, current, "pushEnabled", changedFields);
    updateBooleanField(body, current, "smsEnabled", changedFields);
    updateBooleanField(body, current, "taskAssignedEnabled", changedFields);
    updateBooleanField(body, current, "keyPickupEnabled", changedFields);
    updateBooleanField(body, current, "keyReturnEnabled", changedFields);
    updateBooleanField(body, current, "quietHoursEnabled", changedFields);
    updateTimeField(body, current, "quietHoursStart", changedFields);
    updateTimeField(body, current, "quietHoursEnd", changedFields);

    String now = OffsetDateTime.now().toString();
    current.put("updatedAt", now);
    current.put("requestId", context.requestId());

    notificationPrefsByTenantUser.put(roleModeKey(context.tenantId(), context.userId()), Map.copyOf(current));

    auditService.append(
      "APP_TECH_NOTIFICATION_PREFS_UPDATE",
      "app-tech-notification-prefs:" + context.userId(),
      "SUCCESS",
      Map.of(
        "userId", context.userId(),
        "changedFields", List.copyOf(changedFields)
      )
    );
    return current;
  }

  private Map<String, Object> resolveNotificationPrefs(RequestContext context) {
    Map<String, Object> stored = notificationPrefsByTenantUser.computeIfAbsent(
      roleModeKey(context.tenantId(), context.userId()),
      ignore -> Map.copyOf(defaultNotificationPrefs(context))
    );
    Map<String, Object> view = new LinkedHashMap<>(stored);
    view.put("requestId", context.requestId());
    return view;
  }

  private Map<String, Object> defaultNotificationPrefs(RequestContext context) {
    Map<String, Object> defaults = new LinkedHashMap<>();
    defaults.put("tenantId", context.tenantId());
    defaults.put("userId", context.userId());
    defaults.put("inAppEnabled", true);
    defaults.put("pushEnabled", true);
    defaults.put("smsEnabled", false);
    defaults.put("taskAssignedEnabled", true);
    defaults.put("keyPickupEnabled", true);
    defaults.put("keyReturnEnabled", true);
    defaults.put("quietHoursEnabled", false);
    defaults.put("quietHoursStart", "22:00");
    defaults.put("quietHoursEnd", "07:00");
    defaults.put("updatedAt", "");
    defaults.put("requestId", context.requestId());
    return defaults;
  }

  private Map<String, Object> toProfileView(
    RequestContext context,
    String currentRoleMode,
    boolean canSwitchRoleMode,
    String switchedAt
  ) {
    Map<String, Object> view = new LinkedHashMap<>();
    view.put("tenantId", context.tenantId());
    view.put("userId", context.userId());
    view.put("currentRoleMode", currentRoleMode);
    view.put("availableRoleModes", availableRoleModes(context.userId()));
    view.put("canSwitchRoleMode", canSwitchRoleMode);
    view.put("switchedAt", switchedAt);
    view.put("requestId", context.requestId());
    return view;
  }

  private String currentRoleMode(String tenantId, String userId) {
    return roleModeByTenantUser.computeIfAbsent(
      roleModeKey(tenantId, userId),
      ignore -> defaultRoleMode(userId)
    );
  }

  private String defaultRoleMode(String userId) {
    String normalizedUserId = userId == null ? "" : userId.trim().toLowerCase(Locale.ROOT);
    if (normalizedUserId.startsWith("valet")) {
      return "VALET";
    }
    return "TECHNICIAN";
  }

  private String roleModeKey(String tenantId, String userId) {
    return tenantId + "::" + userId;
  }

  private void updateBooleanField(
    Map<String, Object> source,
    Map<String, Object> target,
    String field,
    List<String> changedFields
  ) {
    if (!source.containsKey(field)) {
      return;
    }

    Object raw = source.get(field);
    if (!(raw instanceof Boolean value)) {
      throw new BadRequestException(field + " must be a boolean");
    }

    if (!Boolean.valueOf(value).equals(target.get(field))) {
      target.put(field, value);
      changedFields.add(field);
    }
  }

  private void updateTimeField(
    Map<String, Object> source,
    Map<String, Object> target,
    String field,
    List<String> changedFields
  ) {
    if (!source.containsKey(field)) {
      return;
    }

    String value = asString(source.get(field)).trim();
    if (value.isBlank()) {
      throw new BadRequestException(field + " cannot be blank");
    }
    if (!HH_MM_PATTERN.matcher(value).matches()) {
      throw new BadRequestException(field + " must match HH:mm");
    }

    if (!value.equals(target.get(field))) {
      target.put(field, value);
      changedFields.add(field);
    }
  }

  private List<String> availableRoleModes(String userId) {
    List<String> modes = new ArrayList<>();
    modes.add(defaultRoleMode(userId));
    if (!modes.contains("VALET")) {
      modes.add("VALET");
    }
    if (!modes.contains("TECHNICIAN")) {
      modes.add("TECHNICIAN");
    }
    return List.copyOf(modes);
  }

  private String normalizeRequiredRoleMode(Map<String, Object> body) {
    String roleMode = asString(body.get("targetRoleMode"));
    if (roleMode.isBlank()) {
      roleMode = asString(body.get("roleMode"));
    }
    if (roleMode.isBlank()) {
      throw new BadRequestException("targetRoleMode is required");
    }
    String normalized = roleMode.trim().toUpperCase(Locale.ROOT);
    if (!SUPPORTED_ROLE_MODES.contains(normalized)) {
      throw new BadRequestException("Unsupported role mode: " + roleMode);
    }
    return normalized;
  }

  private Map<String, Object> toTaskView(Map<String, Object> task) {
    Map<String, Object> view = new LinkedHashMap<>();
    view.put("taskId", asString(task.get("taskId")));
    view.put("orderId", asString(task.get("orderId")));
    view.put("title", asString(task.get("title")));
    view.put("status", asString(task.get("status")));
    view.put("assigneeUserId", asString(task.get("assigneeUserId")));
    view.put("collaborators", normalizeStringList(task.get("collaborators")));
    view.put("createdAt", asString(task.get("createdAt")));
    view.put("updatedAt", asString(task.get("updatedAt")));
    view.put("acceptedAt", asString(task.get("acceptedAt")));
    view.put("completedAt", asString(task.get("completedAt")));
    view.put("requestId", asString(task.get("requestId")));
    return view;
  }

  private List<String> normalizeStringList(Object value) {
    if (value == null) {
      return List.of();
    }
    if (value instanceof List<?> list) {
      return list.stream().map(String::valueOf).toList();
    }
    return List.of(String.valueOf(value));
  }

  private String normalizeStatus(String status) {
    return status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
