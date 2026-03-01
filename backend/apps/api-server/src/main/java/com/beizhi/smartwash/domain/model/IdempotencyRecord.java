package com.beizhi.smartwash.domain.model;

import java.time.OffsetDateTime;

public record IdempotencyRecord(
  String key,
  String tenantId,
  String requestHash,
  Object response,
  OffsetDateTime createdAt
) {}
