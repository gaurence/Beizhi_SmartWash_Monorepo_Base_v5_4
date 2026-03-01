package com.beizhi.smartwash.domain.model;

import java.time.OffsetDateTime;

public record OrgUnit(
  String id,
  String tenantId,
  String orgCode,
  String orgName,
  String orgType,
  String parentOrgId,
  String status,
  OffsetDateTime createdAt,
  OffsetDateTime updatedAt
) {}
