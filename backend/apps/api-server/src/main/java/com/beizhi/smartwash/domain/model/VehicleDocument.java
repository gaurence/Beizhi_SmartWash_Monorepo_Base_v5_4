package com.beizhi.smartwash.domain.model;

import java.time.OffsetDateTime;

public record VehicleDocument(
  String documentId,
  String tenantId,
  String vehicleId,
  String docType,
  String objectKey,
  String verifiedStatus,
  OffsetDateTime uploadedAt,
  OffsetDateTime verifiedAt
) {}
