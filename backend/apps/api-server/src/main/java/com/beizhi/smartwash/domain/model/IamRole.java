package com.beizhi.smartwash.domain.model;

import java.time.OffsetDateTime;
import java.util.Set;

public record IamRole(
  String id,
  String tenantId,
  String roleCode,
  String roleName,
  String scope,
  String status,
  boolean systemRole,
  Set<String> permissionKeys,
  OffsetDateTime createdAt,
  OffsetDateTime updatedAt
) {}
