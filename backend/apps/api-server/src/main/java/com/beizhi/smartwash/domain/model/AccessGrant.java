package com.beizhi.smartwash.domain.model;

import java.time.OffsetDateTime;

public record AccessGrant(
  String id,
  String tenantId,
  String userId,
  String grantType,
  String action,
  String orderId,
  String taskId,
  String cabinetId,
  String slotId,
  String token,
  OffsetDateTime issuedAt,
  OffsetDateTime expiresAt,
  String status
) {}
