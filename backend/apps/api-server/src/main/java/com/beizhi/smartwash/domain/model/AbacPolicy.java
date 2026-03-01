package com.beizhi.smartwash.domain.model;

import java.time.OffsetDateTime;

public record AbacPolicy(
  String id,
  String tenantId,
  String policyCode,
  String name,
  String action,
  String resourceType,
  String conditionExpr,
  boolean enabled,
  OffsetDateTime createdAt,
  OffsetDateTime updatedAt
) {}
