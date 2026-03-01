package com.beizhi.smartwash.domain.model;

import java.time.OffsetDateTime;

public record CustomerProfile(
  String customerId,
  String tenantId,
  String userId,
  String nameMasked,
  String phoneMasked,
  String kycStatus,
  String idCardNoMasked,
  String driverLicenseNoMasked,
  OffsetDateTime kycSubmittedAt,
  OffsetDateTime kycReviewedAt
) {}
