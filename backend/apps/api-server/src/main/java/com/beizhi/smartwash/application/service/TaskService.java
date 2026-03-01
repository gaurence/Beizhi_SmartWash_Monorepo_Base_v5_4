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

@Service
public class TaskService {
  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public TaskService(
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

  public List<Map<String, Object>> listTasks(int page, int size, String status) {
    permissionGuard.check("task.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.listAppTechTasks(tenantId, "", normalizeStatus(status), page, size).stream()
      .map(this::toView)
      .toList();
  }

  public long countTasks(String status) {
    permissionGuard.check("task.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.countAppTechTasks(tenantId, "", normalizeStatus(status));
  }

  public Map<String, Object> assignTask(String taskId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("task.manage");
    RequestContext context = RequestContextHolder.get();
    String assigneeUserId = asString(body.get("assigneeUserId")).trim();
    if (!StringUtils.hasText(assigneeUserId)) {
      throw new BadRequestException("assigneeUserId is required");
    }

    String requestHash = context.tenantId() + "|" + taskId + "|assign|" + assigneeUserId + "|" + String.valueOf(body);
    @SuppressWarnings("unchecked")
    Map<String, Object> assigned = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "task-assign:" + taskId,
      idempotencyKey,
      requestHash,
      () -> doAssign(context, taskId, assigneeUserId)
    );
    return toView(assigned);
  }

  public Map<String, Object> acceptTask(String taskId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("task.manage");
    RequestContext context = RequestContextHolder.get();
    String requestHash = context.tenantId() + "|" + taskId + "|accept|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> accepted = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "task-accept:" + taskId,
      idempotencyKey,
      requestHash,
      () -> doAccept(context, taskId)
    );
    return toView(accepted);
  }

  public Map<String, Object> completeTask(String taskId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("task.manage");
    RequestContext context = RequestContextHolder.get();
    String requestHash = context.tenantId() + "|" + taskId + "|complete|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> completed = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "task-complete:" + taskId,
      idempotencyKey,
      requestHash,
      () -> doComplete(context, taskId)
    );
    return toView(completed);
  }

  private Map<String, Object> doAssign(RequestContext context, String taskId, String assigneeUserId) {
    Map<String, Object> task = repository.findAppTechTask(context.tenantId(), taskId)
      .orElseThrow(() -> new NotFoundException("Task not found"));

    Map<String, Object> updated = new LinkedHashMap<>(task);
    String fromAssignee = asString(task.get("assigneeUserId"));
    String fromStatus = asString(task.get("status")).toUpperCase(Locale.ROOT);
    OffsetDateTime now = OffsetDateTime.now();

    updated.put("assigneeUserId", assigneeUserId);
    if (!"DISPATCHED".equals(fromStatus) && !"ACCEPTED".equals(fromStatus) && !"IN_PROGRESS".equals(fromStatus)) {
      updated.put("status", "DISPATCHED");
    }
    updated.put("updatedAt", now.toString());
    updated.put("requestId", context.requestId());
    repository.saveAppTechTask(context.tenantId(), updated);

    auditService.append(
      "TASK_ASSIGN",
      "task:" + taskId,
      "SUCCESS",
      Map.of("taskId", taskId, "fromAssignee", fromAssignee, "toAssignee", assigneeUserId)
    );
    return updated;
  }

  private Map<String, Object> doAccept(RequestContext context, String taskId) {
    Map<String, Object> task = repository.findAppTechTask(context.tenantId(), taskId)
      .orElseThrow(() -> new NotFoundException("Task not found"));

    String fromStatus = asString(task.get("status")).toUpperCase(Locale.ROOT);
    if ("ACCEPTED".equals(fromStatus)) {
      return task;
    }
    if (!"DISPATCHED".equals(fromStatus)) {
      throw new BadRequestException("Task cannot be accepted in status: " + fromStatus);
    }

    Map<String, Object> updated = new LinkedHashMap<>(task);
    OffsetDateTime now = OffsetDateTime.now();
    updated.put("status", "ACCEPTED");
    updated.put("acceptedAt", now.toString());
    updated.put("updatedAt", now.toString());
    updated.put("requestId", context.requestId());
    repository.saveAppTechTask(context.tenantId(), updated);

    auditService.append(
      "TASK_ACCEPT",
      "task:" + taskId,
      "SUCCESS",
      Map.of("taskId", taskId, "fromStatus", fromStatus, "toStatus", "ACCEPTED")
    );
    return updated;
  }

  private Map<String, Object> doComplete(RequestContext context, String taskId) {
    Map<String, Object> task = repository.findAppTechTask(context.tenantId(), taskId)
      .orElseThrow(() -> new NotFoundException("Task not found"));

    String fromStatus = asString(task.get("status")).toUpperCase(Locale.ROOT);
    if ("COMPLETED".equals(fromStatus)) {
      return task;
    }
    if (!"ACCEPTED".equals(fromStatus) && !"IN_PROGRESS".equals(fromStatus)) {
      throw new BadRequestException("Task cannot be completed in status: " + fromStatus);
    }

    Map<String, Object> updated = new LinkedHashMap<>(task);
    OffsetDateTime now = OffsetDateTime.now();
    updated.put("status", "COMPLETED");
    updated.put("completedAt", now.toString());
    updated.put("updatedAt", now.toString());
    updated.put("requestId", context.requestId());
    repository.saveAppTechTask(context.tenantId(), updated);

    auditService.append(
      "TASK_COMPLETE",
      "task:" + taskId,
      "SUCCESS",
      Map.of("taskId", taskId, "fromStatus", fromStatus, "toStatus", "COMPLETED")
    );
    return updated;
  }

  private Map<String, Object> toView(Map<String, Object> task) {
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
