package com.beizhi.smartwash.domain.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.OffsetDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RefundRequestRecord {
  private final String refundId;
  private final String tenantId;
  private final String orderId;
  private final String createdByUserId;
  private final OffsetDateTime createdAt;
  private double amount;
  private String currency;
  private String reason;
  private String status;
  private OffsetDateTime updatedAt;
  private String approvedByUserId;
  private OffsetDateTime approvedAt;
  private String requestId;

  public RefundRequestRecord(
    String refundId,
    String tenantId,
    String orderId,
    double amount,
    String currency,
    String reason,
    String status,
    String createdByUserId,
    String requestId
  ) {
    this.refundId = refundId;
    this.tenantId = tenantId;
    this.orderId = orderId;
    this.amount = amount;
    this.currency = currency;
    this.reason = reason;
    this.status = status;
    this.createdByUserId = createdByUserId;
    this.requestId = requestId;
    this.createdAt = OffsetDateTime.now();
    this.updatedAt = this.createdAt;
  }

  public String refundId() {
    return refundId;
  }

  public String tenantId() {
    return tenantId;
  }

  public String orderId() {
    return orderId;
  }

  public double amount() {
    return amount;
  }

  public String currency() {
    return currency;
  }

  public String reason() {
    return reason;
  }

  public String status() {
    return status;
  }

  public String createdByUserId() {
    return createdByUserId;
  }

  public OffsetDateTime createdAt() {
    return createdAt;
  }

  public OffsetDateTime updatedAt() {
    return updatedAt;
  }

  public String approvedByUserId() {
    return approvedByUserId;
  }

  public OffsetDateTime approvedAt() {
    return approvedAt;
  }

  public String requestId() {
    return requestId;
  }

  public void approve(String approverUserId, String nextRequestId) {
    OffsetDateTime now = OffsetDateTime.now();
    this.status = "APPROVED";
    this.approvedByUserId = approverUserId;
    this.approvedAt = now;
    this.updatedAt = now;
    this.requestId = nextRequestId;
  }
}
