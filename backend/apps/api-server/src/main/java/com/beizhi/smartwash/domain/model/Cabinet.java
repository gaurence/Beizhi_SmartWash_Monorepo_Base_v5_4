package com.beizhi.smartwash.domain.model;

public record Cabinet(
  String id,
  String tenantId,
  String siteId,
  String serialNo,
  String status
) {}
