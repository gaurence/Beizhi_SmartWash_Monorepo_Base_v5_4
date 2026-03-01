package com.beizhi.smartwash.domain.model;

public record DeviceInvestor(
  String id,
  String tenantId,
  String name,
  String investorType,
  String status
) {}
