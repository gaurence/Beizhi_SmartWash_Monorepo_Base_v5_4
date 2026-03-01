package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.IamPermission;
import com.beizhi.smartwash.domain.model.IamRole;
import com.beizhi.smartwash.domain.model.IamUser;
import com.beizhi.smartwash.infrastructure.repository.InMemoryPlatformRepository;
import com.beizhi.smartwash.interfaces.rest.error.BadRequestException;
import com.beizhi.smartwash.interfaces.rest.error.ConflictException;
import com.beizhi.smartwash.interfaces.rest.error.NotFoundException;
import com.beizhi.smartwash.security.PermissionGuard;
import com.beizhi.smartwash.security.RequestContext;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class RbacService {
  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public RbacService(
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

  public List<IamUser> listUsers(int page, int size) {
    permissionGuard.check("iam.user.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.listUsers(tenantId, page, size);
  }

  public long countUsers() {
    return repository.countUsers(RequestContextHolder.get().tenantId());
  }

  public IamUser getUser(String userId) {
    permissionGuard.check("iam.user.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.findUser(tenantId, userId)
      .orElseThrow(() -> new NotFoundException("User not found"));
  }

  public IamUser createUser(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("iam.user.create");
    RequestContext context = RequestContextHolder.get();

    String username = asString(body.get("username")).trim();
    if (!StringUtils.hasText(username)) {
      throw new BadRequestException("username is required");
    }

    String requestHash = context.tenantId() + "|" + username + "|" + asString(body.get("displayName"));
    return (IamUser) idempotencyService.execute(
      context.tenantId(),
      "iam-user-create",
      idempotencyKey,
      requestHash,
      () -> doCreateUser(context.tenantId(), body)
    );
  }

  public IamUser updateUser(String userId, Map<String, Object> body) {
    permissionGuard.check("iam.user.update");
    RequestContext context = RequestContextHolder.get();
    IamUser existing = repository.findUser(context.tenantId(), userId)
      .orElseThrow(() -> new NotFoundException("User not found"));

    String nextDisplayName = asString(body.get("displayName"));
    String nextOrgId = asString(body.get("orgId"));
    String nextStatus = asString(body.get("status"));
    String nextMobile = asString(body.get("mobile"));

    if (StringUtils.hasText(nextOrgId) && repository.findOrg(context.tenantId(), nextOrgId).isEmpty()) {
      throw new BadRequestException("orgId not found in tenant scope");
    }

    IamUser updated = new IamUser(
      existing.id(),
      existing.tenantId(),
      existing.username(),
      StringUtils.hasText(nextDisplayName) ? nextDisplayName : existing.displayName(),
      StringUtils.hasText(nextOrgId) ? nextOrgId : existing.orgId(),
      StringUtils.hasText(nextStatus) ? nextStatus : existing.status(),
      StringUtils.hasText(nextMobile) ? maskMobile(nextMobile) : existing.mobileMasked(),
      existing.roleIds(),
      existing.createdAt(),
      OffsetDateTime.now()
    );
    repository.saveUser(updated);

    auditService.append(
      "IAM_USER_UPDATE",
      "user:" + userId,
      "SUCCESS",
      Map.of("status", updated.status(), "orgId", updated.orgId())
    );
    return updated;
  }

  public IamUser bindUserRoles(String userId, Map<String, Object> body) {
    permissionGuard.check("iam.user.role.bind");
    RequestContext context = RequestContextHolder.get();
    IamUser existing = repository.findUser(context.tenantId(), userId)
      .orElseThrow(() -> new NotFoundException("User not found"));

    Set<String> roleIds = parseStringSet(body.get("roleIds"));
    if (roleIds.isEmpty()) {
      throw new BadRequestException("roleIds is required");
    }

    for (String roleId : roleIds) {
      if (repository.findRole(context.tenantId(), roleId).isEmpty()) {
        throw new BadRequestException("roleId not found in tenant scope: " + roleId);
      }
    }

    IamUser updated = new IamUser(
      existing.id(),
      existing.tenantId(),
      existing.username(),
      existing.displayName(),
      existing.orgId(),
      existing.status(),
      existing.mobileMasked(),
      Set.copyOf(roleIds),
      existing.createdAt(),
      OffsetDateTime.now()
    );
    repository.saveUser(updated);

    auditService.append(
      "IAM_USER_ROLE_BIND",
      "user:" + userId,
      "SUCCESS",
      Map.of("roleIds", roleIds)
    );
    return updated;
  }

  public List<IamRole> listRoles(int page, int size) {
    permissionGuard.check("iam.role.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.listRoles(tenantId, page, size);
  }

  public long countRoles() {
    return repository.countRoles(RequestContextHolder.get().tenantId());
  }

  public IamRole createRole(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("iam.role.create");
    RequestContext context = RequestContextHolder.get();

    String roleCode = asString(body.get("roleCode")).trim().toUpperCase();
    String roleName = asString(body.get("roleName")).trim();
    if (!StringUtils.hasText(roleCode) || !StringUtils.hasText(roleName)) {
      throw new BadRequestException("roleCode and roleName are required");
    }

    String requestHash = context.tenantId() + "|" + roleCode + "|" + roleName;
    return (IamRole) idempotencyService.execute(
      context.tenantId(),
      "iam-role-create",
      idempotencyKey,
      requestHash,
      () -> doCreateRole(context.tenantId(), body)
    );
  }

  public IamRole updateRole(String roleId, Map<String, Object> body) {
    permissionGuard.check("iam.role.update");
    RequestContext context = RequestContextHolder.get();
    IamRole existing = repository.findRole(context.tenantId(), roleId)
      .orElseThrow(() -> new NotFoundException("Role not found"));

    String roleName = asString(body.get("roleName"));
    String status = asString(body.get("status"));
    String scope = asString(body.get("scope"));

    IamRole updated = new IamRole(
      existing.id(),
      existing.tenantId(),
      existing.roleCode(),
      StringUtils.hasText(roleName) ? roleName : existing.roleName(),
      StringUtils.hasText(scope) ? scope : existing.scope(),
      StringUtils.hasText(status) ? status : existing.status(),
      existing.systemRole(),
      existing.permissionKeys(),
      existing.createdAt(),
      OffsetDateTime.now()
    );
    repository.saveRole(updated);

    auditService.append(
      "IAM_ROLE_UPDATE",
      "role:" + roleId,
      "SUCCESS",
      Map.of("status", updated.status(), "scope", updated.scope())
    );
    return updated;
  }

  public IamRole bindRolePermissions(String roleId, Map<String, Object> body) {
    permissionGuard.check("iam.role.perm.bind");
    RequestContext context = RequestContextHolder.get();
    IamRole existing = repository.findRole(context.tenantId(), roleId)
      .orElseThrow(() -> new NotFoundException("Role not found"));

    Set<String> permissionKeys = parseStringSet(body.get("permissionKeys"));
    if (permissionKeys.isEmpty()) {
      throw new BadRequestException("permissionKeys is required");
    }
    for (String permissionKey : permissionKeys) {
      if (repository.findPermissionByKey(context.tenantId(), permissionKey).isEmpty()) {
        throw new BadRequestException("permissionKey not found in tenant scope: " + permissionKey);
      }
    }

    IamRole updated = new IamRole(
      existing.id(),
      existing.tenantId(),
      existing.roleCode(),
      existing.roleName(),
      existing.scope(),
      existing.status(),
      existing.systemRole(),
      Set.copyOf(permissionKeys),
      existing.createdAt(),
      OffsetDateTime.now()
    );
    repository.saveRole(updated);

    auditService.append(
      "IAM_ROLE_PERMISSION_BIND",
      "role:" + roleId,
      "SUCCESS",
      Map.of("permissionKeys", permissionKeys)
    );
    return updated;
  }

  public List<IamPermission> listPermissions() {
    permissionGuard.check("iam.perm.read");
    return repository.listPermissions(RequestContextHolder.get().tenantId());
  }

  private IamUser doCreateUser(String tenantId, Map<String, Object> body) {
    String username = asString(body.get("username")).trim();
    boolean exists = repository.listUsers(tenantId, 1, Integer.MAX_VALUE).stream()
      .anyMatch(user -> user.username().equalsIgnoreCase(username));
    if (exists) {
      throw new ConflictException("username already exists in tenant");
    }

    String displayName = asString(body.get("displayName")).trim();
    String orgId = asString(body.get("orgId")).trim();
    String mobile = asString(body.get("mobile")).trim();

    if (StringUtils.hasText(orgId) && repository.findOrg(tenantId, orgId).isEmpty()) {
      throw new BadRequestException("orgId not found in tenant scope");
    }

    Set<String> roleIds = parseStringSet(body.get("roleIds"));
    for (String roleId : roleIds) {
      if (repository.findRole(tenantId, roleId).isEmpty()) {
        throw new BadRequestException("roleId not found in tenant scope: " + roleId);
      }
    }

    OffsetDateTime now = OffsetDateTime.now();
    IamUser user = new IamUser(
      UUID.randomUUID().toString(),
      tenantId,
      username,
      StringUtils.hasText(displayName) ? displayName : username,
      orgId,
      "ACTIVE",
      maskMobile(mobile),
      roleIds.isEmpty() ? Set.of() : Set.copyOf(roleIds),
      now,
      now
    );
    repository.saveUser(user);

    auditService.append(
      "IAM_USER_CREATE",
      "user:" + user.id(),
      "SUCCESS",
      Map.of("username", username, "orgId", orgId, "roleIds", user.roleIds())
    );
    return user;
  }

  private IamRole doCreateRole(String tenantId, Map<String, Object> body) {
    String roleCode = asString(body.get("roleCode")).trim().toUpperCase();
    String roleName = asString(body.get("roleName")).trim();
    boolean exists = repository.listRoles(tenantId, 1, Integer.MAX_VALUE).stream()
      .anyMatch(role -> role.roleCode().equalsIgnoreCase(roleCode));
    if (exists) {
      throw new ConflictException("roleCode already exists in tenant");
    }

    Set<String> permissionKeys = parseStringSet(body.get("permissionKeys"));
    for (String permissionKey : permissionKeys) {
      if (repository.findPermissionByKey(tenantId, permissionKey).isEmpty()) {
        throw new BadRequestException("permissionKey not found in tenant scope: " + permissionKey);
      }
    }

    OffsetDateTime now = OffsetDateTime.now();
    IamRole role = new IamRole(
      UUID.randomUUID().toString(),
      tenantId,
      roleCode,
      roleName,
      StringUtils.hasText(asString(body.get("scope"))) ? asString(body.get("scope")) : "TENANT",
      "ACTIVE",
      false,
      permissionKeys,
      now,
      now
    );
    repository.saveRole(role);

    auditService.append(
      "IAM_ROLE_CREATE",
      "role:" + role.id(),
      "SUCCESS",
      Map.of("roleCode", roleCode, "permissionKeys", permissionKeys)
    );
    return role;
  }

  private Set<String> parseStringSet(Object value) {
    if (value == null) {
      return Set.of();
    }
    if (value instanceof List<?> list) {
      LinkedHashSet<String> result = new LinkedHashSet<>();
      for (Object item : list) {
        String str = asString(item).trim();
        if (StringUtils.hasText(str)) {
          result.add(str);
        }
      }
      return result;
    }
    String raw = asString(value);
    if (!StringUtils.hasText(raw)) {
      return Set.of();
    }
    LinkedHashSet<String> result = new LinkedHashSet<>();
    for (String item : raw.split(",")) {
      String str = item.trim();
      if (StringUtils.hasText(str)) {
        result.add(str);
      }
    }
    return result;
  }

  private String maskMobile(String value) {
    if (!StringUtils.hasText(value)) {
      return "";
    }
    String normalized = value.replaceAll("\\s+", "");
    if (normalized.length() < 7) {
      return "***";
    }
    String prefix = normalized.substring(0, 3);
    String suffix = normalized.substring(normalized.length() - 4);
    return prefix + "****" + suffix;
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
