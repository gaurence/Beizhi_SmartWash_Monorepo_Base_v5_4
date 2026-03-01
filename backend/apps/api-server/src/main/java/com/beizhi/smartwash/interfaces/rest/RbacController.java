package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.RbacService;
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
public class RbacController {
  private final RbacService rbacService;

  public RbacController(RbacService rbacService) {
    this.rbacService = rbacService;
  }

  @GetMapping("/users")
  public PageResponse listUsers(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(rbacService.listUsers(page, size), page, size, rbacService.countUsers());
  }

  @PostMapping("/users")
  public ApiResponse createUser(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(rbacService.createUser(body == null ? Map.of() : body, idempotencyKey));
  }

  @GetMapping("/users/{userId}")
  public ApiResponse getUser(@PathVariable("userId") String userId) {
    return ApiResponse.ok(rbacService.getUser(userId));
  }

  @PutMapping("/users/{userId}")
  public ApiResponse updateUser(@PathVariable("userId") String userId, @RequestBody(required = false) Map<String, Object> body) {
    return ApiResponse.ok(rbacService.updateUser(userId, body == null ? Map.of() : body));
  }

  @PutMapping("/users/{userId}/roles")
  public ApiResponse bindUserRoles(@PathVariable("userId") String userId, @RequestBody(required = false) Map<String, Object> body) {
    return ApiResponse.ok(rbacService.bindUserRoles(userId, body == null ? Map.of() : body));
  }

  @GetMapping("/roles")
  public PageResponse listRoles(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(rbacService.listRoles(page, size), page, size, rbacService.countRoles());
  }

  @PostMapping("/roles")
  public ApiResponse createRole(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(rbacService.createRole(body == null ? Map.of() : body, idempotencyKey));
  }

  @PutMapping("/roles/{roleId}")
  public ApiResponse updateRole(@PathVariable("roleId") String roleId, @RequestBody(required = false) Map<String, Object> body) {
    return ApiResponse.ok(rbacService.updateRole(roleId, body == null ? Map.of() : body));
  }

  @PutMapping("/roles/{roleId}/permissions")
  public ApiResponse bindRolePermissions(@PathVariable("roleId") String roleId, @RequestBody(required = false) Map<String, Object> body) {
    return ApiResponse.ok(rbacService.bindRolePermissions(roleId, body == null ? Map.of() : body));
  }

  @GetMapping("/permissions")
  public ApiResponse listPermissions() {
    return ApiResponse.ok(rbacService.listPermissions());
  }
}
