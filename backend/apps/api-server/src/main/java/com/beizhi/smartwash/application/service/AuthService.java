package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class AuthService {
  public Map<String, Object> login(Map<String, Object> body) {
    String username = asString(body.get("username"));
    String role = asString(body.get("role"));

    if (!StringUtils.hasText(username)) {
      username = "mock-user";
    }
    if (!StringUtils.hasText(role)) {
      role = inferRole(username);
    }

    String tenantId = RequestContextHolder.get().tenantId();
    String token = "mock-jwt:" + tenantId + ":" + username;

    return Map.of(
      "token", token,
      "tokenType", "Bearer",
      "expiresIn", 3600,
      "tenantId", tenantId,
      "username", username,
      "role", role,
      "issuedAt", OffsetDateTime.now().toString()
    );
  }

  public Map<String, Object> verifyMfa(Map<String, Object> body) {
    return Map.of(
      "verified", true,
      "method", asString(body.get("method")),
      "verifiedAt", OffsetDateTime.now().toString()
    );
  }

  public Map<String, Object> logout() {
    return Map.of(
      "loggedOut", true,
      "requestId", RequestContextHolder.get().requestId(),
      "loggedOutAt", OffsetDateTime.now().toString()
    );
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private String inferRole(String username) {
    return switch (username) {
      case "pa_admin" -> "PLATFORM_ADMIN";
      case "ops_admin" -> "PLATFORM_OPS";
      case "fin_admin" -> "PLATFORM_FINANCE";
      case "iot_admin" -> "PLATFORM_IOTOPS";
      case "cs_admin" -> "PLATFORM_CS";
      case "agent_admin" -> "AGENT_ADMIN";
      case "store_admin" -> "STORE_ADMIN";
      case "investor_admin" -> "INVESTOR_ADMIN";
      case "property_admin" -> "PROPERTY_ADMIN";
      case "tech01" -> "TECHNICIAN";
      case "valet01" -> "VALET";
      default -> "PLATFORM_ADMIN";
    };
  }
}
