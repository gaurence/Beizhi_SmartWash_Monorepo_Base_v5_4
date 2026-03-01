package com.beizhi.smartwash.domain.model;

import java.time.OffsetDateTime;

public record TenantInfo(
  String id,
  String tenantCode,
  String tenantName,
  String status,
  OffsetDateTime createdAt,
  OffsetDateTime updatedAt
) {}
