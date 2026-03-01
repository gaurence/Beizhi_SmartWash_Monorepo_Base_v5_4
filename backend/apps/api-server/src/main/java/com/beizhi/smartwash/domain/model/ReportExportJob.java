package com.beizhi.smartwash.domain.model;

import java.time.OffsetDateTime;
import java.util.Map;

public record ReportExportJob(
  String exportId,
  String tenantId,
  String viewScope,
  String status,
  String format,
  String downloadUrl,
  String requestId,
  OffsetDateTime createdAt,
  Map<String, Object> filters
) {}
