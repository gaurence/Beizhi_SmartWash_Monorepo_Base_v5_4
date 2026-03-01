package com.beizhi.smartwash.domain.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.OffsetDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CabinetStoreBinding {
  private final String id;
  private final String tenantId;
  private final String cabinetId;
  private String storeId;
  private String bindingType;
  private String status;
  private final OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private String approvedBy;

  public CabinetStoreBinding(String id, String tenantId, String cabinetId, String storeId, String bindingType, String status) {
    this.id = id;
    this.tenantId = tenantId;
    this.cabinetId = cabinetId;
    this.storeId = storeId;
    this.bindingType = bindingType;
    this.status = status;
    this.createdAt = OffsetDateTime.now();
    this.updatedAt = this.createdAt;
  }

  public String id() { return id; }

  public String tenantId() { return tenantId; }

  public String cabinetId() { return cabinetId; }

  public String storeId() { return storeId; }

  public String bindingType() { return bindingType; }

  public String status() { return status; }

  public OffsetDateTime createdAt() { return createdAt; }

  public OffsetDateTime updatedAt() { return updatedAt; }

  public String approvedBy() { return approvedBy; }

  public void update(String nextStoreId, String nextBindingType, String nextStatus) {
    if (nextStoreId != null && !nextStoreId.isBlank()) {
      this.storeId = nextStoreId;
    }
    if (nextBindingType != null && !nextBindingType.isBlank()) {
      this.bindingType = nextBindingType;
    }
    if (nextStatus != null && !nextStatus.isBlank()) {
      this.status = nextStatus;
    }
    this.updatedAt = OffsetDateTime.now();
  }

  public void approve(String approverUserId) {
    this.status = "APPROVED";
    this.approvedBy = approverUserId;
    this.updatedAt = OffsetDateTime.now();
  }
}
