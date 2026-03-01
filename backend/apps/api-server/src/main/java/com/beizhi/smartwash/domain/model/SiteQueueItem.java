package com.beizhi.smartwash.domain.model;

public record SiteQueueItem(
  String queueNo,
  String serviceType,
  int etaMinutes,
  String status
) {}
