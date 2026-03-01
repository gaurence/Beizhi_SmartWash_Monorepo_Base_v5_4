package com.beizhi.smartwash.domain.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.OffsetDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TicketRecord {
  private final String ticketId;
  private final String tenantId;
  private final String orderId;
  private final String sourceChannel;
  private final String category;
  private final String priority;
  private final String createdByUserId;
  private final OffsetDateTime createdAt;
  private String status;
  private String summary;
  private String description;
  private int evidenceCount;
  private String assigneeUserId;
  private OffsetDateTime updatedAt;
  private OffsetDateTime closedAt;
  private String requestId;

  public TicketRecord(
    String ticketId,
    String tenantId,
    String orderId,
    String sourceChannel,
    String category,
    String priority,
    String status,
    String summary,
    String description,
    int evidenceCount,
    String createdByUserId,
    String assigneeUserId,
    String requestId
  ) {
    this.ticketId = ticketId;
    this.tenantId = tenantId;
    this.orderId = orderId;
    this.sourceChannel = sourceChannel;
    this.category = category;
    this.priority = priority;
    this.status = status;
    this.summary = summary;
    this.description = description;
    this.evidenceCount = evidenceCount;
    this.createdByUserId = createdByUserId;
    this.assigneeUserId = assigneeUserId;
    this.requestId = requestId;
    this.createdAt = OffsetDateTime.now();
    this.updatedAt = this.createdAt;
  }

  public String ticketId() {
    return ticketId;
  }

  public String tenantId() {
    return tenantId;
  }

  public String orderId() {
    return orderId;
  }

  public String sourceChannel() {
    return sourceChannel;
  }

  public String category() {
    return category;
  }

  public String priority() {
    return priority;
  }

  public String status() {
    return status;
  }

  public String summary() {
    return summary;
  }

  public String description() {
    return description;
  }

  public int evidenceCount() {
    return evidenceCount;
  }

  public String createdByUserId() {
    return createdByUserId;
  }

  public String assigneeUserId() {
    return assigneeUserId;
  }

  public OffsetDateTime createdAt() {
    return createdAt;
  }

  public OffsetDateTime updatedAt() {
    return updatedAt;
  }

  public OffsetDateTime closedAt() {
    return closedAt;
  }

  public String requestId() {
    return requestId;
  }
}
