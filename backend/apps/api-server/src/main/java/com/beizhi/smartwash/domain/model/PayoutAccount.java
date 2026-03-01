package com.beizhi.smartwash.domain.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.OffsetDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PayoutAccount {
  private final String id;
  private final String tenantId;
  private String ownerOrgId;
  private String accountType;
  private String accountNoMasked;
  private String accountNoEnc;
  private String holderName;
  private String channelMerchantId;
  private String status;
  private final OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private String submittedBy;
  private OffsetDateTime submittedAt;

  public PayoutAccount(
    String id,
    String tenantId,
    String ownerOrgId,
    String accountType,
    String accountNoMasked,
    String accountNoEnc,
    String holderName,
    String channelMerchantId,
    String status
  ) {
    this.id = id;
    this.tenantId = tenantId;
    this.ownerOrgId = ownerOrgId;
    this.accountType = accountType;
    this.accountNoMasked = accountNoMasked;
    this.accountNoEnc = accountNoEnc;
    this.holderName = holderName;
    this.channelMerchantId = channelMerchantId;
    this.status = status;
    this.createdAt = OffsetDateTime.now();
    this.updatedAt = this.createdAt;
  }

  public String id() { return id; }

  public String tenantId() { return tenantId; }

  public String ownerOrgId() { return ownerOrgId; }

  public String accountType() { return accountType; }

  public String accountNoMasked() { return accountNoMasked; }

  public String accountNoEnc() { return accountNoEnc; }

  public String holderName() { return holderName; }

  public String channelMerchantId() { return channelMerchantId; }

  public String status() { return status; }

  public OffsetDateTime createdAt() { return createdAt; }

  public OffsetDateTime updatedAt() { return updatedAt; }

  public String submittedBy() { return submittedBy; }

  public OffsetDateTime submittedAt() { return submittedAt; }

  public void update(
    String nextOwnerOrgId,
    String nextAccountType,
    String nextAccountNoMasked,
    String nextAccountNoEnc,
    String nextHolderName,
    String nextChannelMerchantId,
    String nextStatus
  ) {
    if (nextOwnerOrgId != null && !nextOwnerOrgId.isBlank()) {
      this.ownerOrgId = nextOwnerOrgId;
    }
    if (nextAccountType != null && !nextAccountType.isBlank()) {
      this.accountType = nextAccountType;
    }
    if (nextAccountNoMasked != null && !nextAccountNoMasked.isBlank()) {
      this.accountNoMasked = nextAccountNoMasked;
    }
    if (nextAccountNoEnc != null && !nextAccountNoEnc.isBlank()) {
      this.accountNoEnc = nextAccountNoEnc;
    }
    if (nextHolderName != null && !nextHolderName.isBlank()) {
      this.holderName = nextHolderName;
    }
    if (nextChannelMerchantId != null && !nextChannelMerchantId.isBlank()) {
      this.channelMerchantId = nextChannelMerchantId;
    }
    if (nextStatus != null && !nextStatus.isBlank()) {
      this.status = nextStatus;
    }
    this.updatedAt = OffsetDateTime.now();
  }

  public void submitReview(String submitterUserId) {
    this.status = "VERIFYING";
    this.submittedBy = submitterUserId;
    this.submittedAt = OffsetDateTime.now();
    this.updatedAt = this.submittedAt;
  }
}
