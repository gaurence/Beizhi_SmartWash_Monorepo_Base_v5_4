package com.beizhi.smartwash.domain.model;

import java.time.OffsetDateTime;
import java.util.Map;

public record ReportSnapshot(
  String snapshotId,
  String tenantId,
  String viewScope,
  String period,
  OffsetDateTime generatedAt,
  Map<String, Object> metrics
) {}
