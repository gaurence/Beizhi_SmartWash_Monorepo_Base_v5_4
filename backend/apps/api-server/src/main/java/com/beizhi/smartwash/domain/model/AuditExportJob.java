package com.beizhi.smartwash.domain.model;

import java.time.OffsetDateTime;
import java.util.Map;

public record AuditExportJob(
  String id,
  String tenantId,
  String requestId,
  String createdBy,
  String status,
  String downloadPath,
  OffsetDateTime createdAt,
  Map<String, Object> filters
) {}
