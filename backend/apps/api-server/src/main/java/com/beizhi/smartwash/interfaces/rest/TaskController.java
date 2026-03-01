package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.TaskService;
import com.beizhi.smartwash.common.ApiResponse;
import com.beizhi.smartwash.common.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TaskController {
  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @GetMapping("/tasks")
  public PageResponse listTasks(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size,
    @RequestParam(name = "status", required = false) String status
  ) {
    return PageResponse.of(taskService.listTasks(page, size, status), page, size, taskService.countTasks(status));
  }

  @PostMapping("/tasks/{taskId}/assign")
  public ApiResponse assignTask(
    @PathVariable("taskId") String taskId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(taskService.assignTask(taskId, body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/tasks/{taskId}/accept")
  public ApiResponse acceptTask(
    @PathVariable("taskId") String taskId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(taskService.acceptTask(taskId, body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/tasks/{taskId}/complete")
  public ApiResponse completeTask(
    @PathVariable("taskId") String taskId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(taskService.completeTask(taskId, body == null ? Map.of() : body, idempotencyKey));
  }
}
