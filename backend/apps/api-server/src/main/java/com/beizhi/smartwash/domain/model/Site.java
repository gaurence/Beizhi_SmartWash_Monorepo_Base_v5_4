package com.beizhi.smartwash.domain.model;

public record Site(
  String id,
  String tenantId,
  String name,
  String district,
  double distanceKm,
  int queueLength,
  String status
) {}
