package com.beizhi.smartwash.domain.model;

import java.time.OffsetDateTime;
import java.util.Set;

public record IamUser(
  String id,
  String tenantId,
  String username,
  String displayName,
  String orgId,
  String status,
  String mobileMasked,
  Set<String> roleIds,
  OffsetDateTime createdAt,
  OffsetDateTime updatedAt
) {}
