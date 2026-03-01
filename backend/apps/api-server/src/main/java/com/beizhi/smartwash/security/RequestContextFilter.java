package com.beizhi.smartwash.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("tenantRequestContextFilter")
public class RequestContextFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {

    String bearerToken = parseBearerToken(request.getHeader("Authorization"));
    String requestId = headerOrDefault(request, "X-Request-Id", UUID.randomUUID().toString());
    String tenantId = resolveTenantId(request, bearerToken);
    String userId = resolveUserId(request, bearerToken);
    Set<String> permissions = parsePermissions(request.getHeader("X-Permissions"));

    RequestContext context = new RequestContext(
      requestId,
      tenantId,
      userId,
      permissions,
      request.getRemoteAddr(),
      request.getHeader("User-Agent")
    );

    RequestContextHolder.set(context);
    response.setHeader("X-Request-Id", requestId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      RequestContextHolder.clear();
    }
  }

  private String headerOrDefault(HttpServletRequest request, String headerName, String defaultValue) {
    String value = request.getHeader(headerName);
    return StringUtils.hasText(value) ? value.trim() : defaultValue;
  }

  private String resolveTenantId(HttpServletRequest request, String bearerToken) {
    String fromToken = parseTokenField(bearerToken, 1);
    if (StringUtils.hasText(fromToken)) {
      return fromToken;
    }
    return "tenant-public";
  }

  private String resolveUserId(HttpServletRequest request, String bearerToken) {
    String fromToken = parseTokenField(bearerToken, 2);
    if (StringUtils.hasText(fromToken)) {
      return fromToken;
    }

    return "anonymous";
  }

  private String parseBearerToken(String authorizationHeader) {
    if (!StringUtils.hasText(authorizationHeader)) {
      return "";
    }

    String value = authorizationHeader.trim();
    if (value.regionMatches(true, 0, "Bearer ", 0, 7) && value.length() > 7) {
      return value.substring(7).trim();
    }

    return "";
  }

  private String parseTokenField(String bearerToken, int index) {
    if (!StringUtils.hasText(bearerToken)) {
      return "";
    }

    String[] segments = bearerToken.split(":", 4);
    if (segments.length < 3) {
      return "";
    }
    if (!"mock-jwt".equals(segments[0]) && !"mock-app-jwt".equals(segments[0])) {
      return "";
    }
    if (index < 1 || index >= segments.length) {
      return "";
    }

    return segments[index].trim();
  }

  private Set<String> parsePermissions(String headerValue) {
    if (!StringUtils.hasText(headerValue)) {
      return Set.of();
    }

    return Arrays.stream(headerValue.split(","))
      .map(String::trim)
      .filter(StringUtils::hasText)
      .collect(Collectors.toUnmodifiableSet());
  }
}
