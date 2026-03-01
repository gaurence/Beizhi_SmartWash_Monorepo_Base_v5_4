package com.beizhi.smartwash.domain.model;

public record IamPermission(
  String id,
  String tenantId,
  String permissionKey,
  String permissionName,
  String resourceType
) {}
