package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.AppTechService;
import com.beizhi.smartwash.common.ApiResponse;
import com.beizhi.smartwash.common.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AppTechController {
  private final AppTechService appTechService;

  public AppTechController(AppTechService appTechService) {
    this.appTechService = appTechService;
  }

  @GetMapping("/app/tech/profile")
  public ApiResponse getProfile() {
    return ApiResponse.ok(appTechService.getProfile());
  }

  @GetMapping("/app/tech/notification-prefs")
  public ApiResponse getNotificationPrefs() {
    return ApiResponse.ok(appTechService.getNotificationPrefs());
  }

  @PutMapping("/app/tech/notification-prefs")
  public ApiResponse updateNotificationPrefs(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(appTechService.updateNotificationPrefs(body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/app/tech/role-mode-switch")
  public ApiResponse switchRoleMode(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(appTechService.switchRoleMode(body == null ? Map.of() : body, idempotencyKey));
  }

  @GetMapping("/app/tech/tasks")
  public PageResponse listTasks(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size,
    @RequestParam(name = "status", required = false) String status
  ) {
    return PageResponse.of(
      appTechService.listTasks(page, size, status),
      page,
      size,
      appTechService.countTasks(status)
    );
  }

  @PostMapping("/app/tech/tasks/{taskId}/accept")
  public ApiResponse acceptTask(
    @PathVariable("taskId") String taskId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(appTechService.acceptTask(taskId, body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/app/tech/tasks/{taskId}/complete")
  public ApiResponse completeTask(
    @PathVariable("taskId") String taskId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(appTechService.completeTask(taskId, body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/app/tech/orders/{orderId}/pickup-key")
  public ApiResponse pickupKey(
    @PathVariable("orderId") String orderId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(appTechService.pickupKey(orderId, body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/app/tech/orders/{orderId}/return-key")
  public ApiResponse returnKey(
    @PathVariable("orderId") String orderId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(appTechService.returnKey(orderId, body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/app/tech/evidence/upload-url")
  public ApiResponse issueEvidenceUploadUrl(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(appTechService.issueEvidenceUploadUrl(body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/app/tech/evidence/records")
  public ApiResponse appendEvidenceRecord(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(appTechService.appendEvidenceRecord(body == null ? Map.of() : body, idempotencyKey));
  }
}
