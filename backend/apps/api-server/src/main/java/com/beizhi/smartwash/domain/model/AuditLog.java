package com.beizhi.smartwash.domain.model;

import java.time.OffsetDateTime;
import java.util.Map;

public record AuditLog(
  String id,
  String requestId,
  String tenantId,
  String actorUserId,
  String action,
  String resource,
  String result,
  String clientIp,
  String userAgent,
  OffsetDateTime createdAt,
  Map<String, Object> details
) {}
