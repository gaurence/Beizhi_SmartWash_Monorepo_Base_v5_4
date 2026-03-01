package com.beizhi.smartwash.security;

import java.util.Set;

public record RequestContext(
  String requestId,
  String tenantId,
  String userId,
  Set<String> permissions,
  String clientIp,
  String userAgent
) {
  public boolean hasPermission(String permission) {
    return permissions != null && permissions.contains(permission);
  }
}
