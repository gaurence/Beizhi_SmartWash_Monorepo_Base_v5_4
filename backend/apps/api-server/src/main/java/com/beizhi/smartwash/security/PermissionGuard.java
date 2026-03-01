package com.beizhi.smartwash.security;

import com.beizhi.smartwash.interfaces.rest.error.ForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class PermissionGuard {
  public void check(String permissionKey) {
    RequestContext context = RequestContextHolder.get();
    if (!context.hasPermission(permissionKey)) {
      throw new ForbiddenException("Missing permission: " + permissionKey);
    }
  }
}
