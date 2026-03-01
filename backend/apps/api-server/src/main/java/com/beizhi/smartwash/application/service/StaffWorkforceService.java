package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.infrastructure.repository.InMemoryPlatformRepository;
import com.beizhi.smartwash.interfaces.rest.error.BadRequestException;
import com.beizhi.smartwash.interfaces.rest.error.ConflictException;
import com.beizhi.smartwash.interfaces.rest.error.NotFoundException;
import com.beizhi.smartwash.security.PermissionGuard;
import com.beizhi.smartwash.security.RequestContext;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class StaffWorkforceService {
  private static final Set<String> SUPPORTED_TYPES = Set.of("TECHNICIAN", "VALET");

  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public StaffWorkforceService(
    InMemoryPlatformRepository repository,
    PermissionGuard permissionGuard,
    IdempotencyService idempotencyService,
    AuditService auditService
  ) {
    this.repository = repository;
    this.permissionGuard = permissionGuard;
    this.idempotencyService = idempotencyService;
    this.auditService = auditService;
  }

  public List<Map<String, Object>> listStaff(String staffType, int page, int size) {
    permissionGuard.check("workforce.staff.read");
    RequestContext context = RequestContextHolder.get();
    String normalizedType = normalizeOptionalStaffType(staffType);
    return repository.listStaff(context.tenantId(), normalizedType, page, size).stream()
      .map(this::toView)
      .toList();
  }

  public long countStaff(String staffType) {
    String normalizedType = normalizeOptionalStaffType(staffType);
    return repository.countStaff(RequestContextHolder.get().tenantId(), normalizedType);
  }

  public Map<String, Object> getStaff(String staffId) {
    permissionGuard.check("workforce.staff.read");
    String tenantId = RequestContextHolder.get().tenantId();
    Map<String, Object> staff = repository.findStaff(tenantId, staffId)
      .orElseThrow(() -> new NotFoundException("Staff not found"));
    return toView(staff);
  }

  public Map<String, Object> createStaff(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("workforce.staff.create");
    RequestContext context = RequestContextHolder.get();

    String staffType = normalizeRequiredStaffType(asString(body.get("staffType")));
    String displayName = asString(body.get("displayName")).trim();
    if (!StringUtils.hasText(displayName)) {
      throw new BadRequestException("displayName is required");
    }

    String employeeCode = asString(body.get("employeeCode")).trim();
    if (!StringUtils.hasText(employeeCode)) {
      employeeCode = generateEmployeeCode(staffType);
    }

    String status = normalizeStatus(asString(body.get("status")));
    String mobileMasked = maskMobile(asString(body.get("mobile")));
    String requestHash = context.tenantId()
      + "|" + staffType
      + "|" + employeeCode.toUpperCase(Locale.ROOT)
      + "|" + displayName
      + "|" + status;

    final String codeForCreate = employeeCode;
    final String statusForCreate = status;
    final String mobileMaskedForCreate = mobileMasked;

    @SuppressWarnings("unchecked")
    Map<String, Object> created = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "workforce-staff-create",
      idempotencyKey,
      requestHash,
      () -> doCreateStaff(context, staffType, codeForCreate, displayName, statusForCreate, mobileMaskedForCreate)
    );
    return toView(created);
  }

  private Map<String, Object> doCreateStaff(
    RequestContext context,
    String staffType,
    String employeeCode,
    String displayName,
    String status,
    String mobileMasked
  ) {
    boolean codeExists = repository.listStaff(context.tenantId(), "", 1, Integer.MAX_VALUE).stream()
      .anyMatch(item -> employeeCode.equalsIgnoreCase(asString(item.get("employeeCode"))));
    if (codeExists) {
      throw new ConflictException("employeeCode already exists in tenant");
    }

    OffsetDateTime now = OffsetDateTime.now();
    String staffId = UUID.randomUUID().toString();

    Map<String, Object> staff = new LinkedHashMap<>();
    staff.put("staffId", staffId);
    staff.put("tenantId", context.tenantId());
    staff.put("staffType", staffType);
    staff.put("employeeCode", employeeCode.toUpperCase(Locale.ROOT));
    staff.put("displayName", displayName);
    staff.put("status", status);
    staff.put("mobileMasked", mobileMasked);
    staff.put("createdAt", now.toString());
    staff.put("updatedAt", now.toString());

    repository.saveStaff(context.tenantId(), staff);
    auditService.append(
      "WORKFORCE_STAFF_CREATE",
      "staff:" + staffId,
      "SUCCESS",
      Map.of("staffId", staffId, "staffType", staffType, "status", status)
    );
    return staff;
  }

  private Map<String, Object> toView(Map<String, Object> staff) {
    return Map.of(
      "staffId", asString(staff.get("staffId")),
      "staffType", asString(staff.get("staffType")),
      "employeeCode", asString(staff.get("employeeCode")),
      "displayName", asString(staff.get("displayName")),
      "status", asString(staff.get("status")),
      "mobileMasked", asString(staff.get("mobileMasked")),
      "createdAt", asString(staff.get("createdAt")),
      "updatedAt", asString(staff.get("updatedAt"))
    );
  }

  private String normalizeOptionalStaffType(String rawType) {
    String value = rawType == null ? "" : rawType.trim();
    if (!StringUtils.hasText(value)) {
      return "";
    }
    String normalized = value.toUpperCase(Locale.ROOT);
    if (!SUPPORTED_TYPES.contains(normalized)) {
      throw new BadRequestException("Unsupported staffType: " + rawType);
    }
    return normalized;
  }

  private String normalizeRequiredStaffType(String rawType) {
    String normalized = normalizeOptionalStaffType(rawType);
    if (!StringUtils.hasText(normalized)) {
      throw new BadRequestException("staffType is required");
    }
    return normalized;
  }

  private String normalizeStatus(String rawStatus) {
    String status = rawStatus == null ? "" : rawStatus.trim();
    if (!StringUtils.hasText(status)) {
      return "ACTIVE";
    }
    return status.toUpperCase(Locale.ROOT);
  }

  private String generateEmployeeCode(String staffType) {
    String prefix = "TECHNICIAN".equals(staffType) ? "TECH" : "VALET";
    return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
  }

  private String maskMobile(String mobile) {
    String value = mobile == null ? "" : mobile.trim();
    if (!StringUtils.hasText(value)) {
      return "";
    }
    if (!value.matches("^\\d{11}$")) {
      throw new BadRequestException("mobile must be 11 digits");
    }
    return value.substring(0, 3) + "****" + value.substring(7);
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
