package com.beizhi.smartwash.domain.model;

public record Vehicle(
  String vehicleId,
  String tenantId,
  String customerId,
  String plateNoMasked,
  String vinMasked,
  String brand,
  String model,
  String color,
  Boolean isDefault
) {}
