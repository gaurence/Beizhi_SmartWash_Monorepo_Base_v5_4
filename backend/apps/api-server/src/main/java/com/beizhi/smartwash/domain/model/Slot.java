package com.beizhi.smartwash.domain.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Slot {
  private final String id;
  private final String tenantId;
  private final String cabinetId;
  private final String slotNo;
  private SlotState state;
  private long rowVersion;

  public Slot(String id, String tenantId, String cabinetId, String slotNo, SlotState state) {
    this.id = id;
    this.tenantId = tenantId;
    this.cabinetId = cabinetId;
    this.slotNo = slotNo;
    this.state = state;
    this.rowVersion = 0L;
  }

  public String id() { return id; }

  public String tenantId() { return tenantId; }

  public String cabinetId() { return cabinetId; }

  public String slotNo() { return slotNo; }

  public SlotState state() { return state; }

  public long rowVersion() { return rowVersion; }

  public void transitionTo(SlotState nextState) {
    this.state = nextState;
    this.rowVersion += 1;
  }
}
