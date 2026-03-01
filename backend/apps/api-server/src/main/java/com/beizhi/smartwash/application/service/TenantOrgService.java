package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.OrgUnit;
import com.beizhi.smartwash.domain.model.TenantInfo;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TenantOrgService {
  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public TenantOrgService(
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

  public List<TenantInfo> listTenants(int page, int size) {
    permissionGuard.check("platform.tenant.read");
    RequestContext context = RequestContextHolder.get();
    if (context.hasPermission("platform.tenant.cross.read")) {
      return repository.listTenants(page, size);
    }
    TenantInfo tenant = repository.findTenant(context.tenantId())
      .orElseThrow(() -> new NotFoundException("Tenant not found"));
    return List.of(tenant);
  }

  public long countTenants() {
    RequestContext context = RequestContextHolder.get();
    if (context.hasPermission("platform.tenant.cross.read")) {
      return repository.countTenants();
    }
    return repository.findTenant(context.tenantId()).isPresent() ? 1L : 0L;
  }

  public TenantInfo createTenant(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("platform.tenant.create");
    RequestContext context = RequestContextHolder.get();

    String tenantCode = asString(body.get("tenantCode")).trim().toUpperCase();
    String tenantName = asString(body.get("tenantName")).trim();
    if (!StringUtils.hasText(tenantCode)) {
      throw new BadRequestException("tenantCode is required");
    }
    if (!StringUtils.hasText(tenantName)) {
      throw new BadRequestException("tenantName is required");
    }

    String requestHash = tenantCode + "|" + tenantName;
    return (TenantInfo) idempotencyService.execute(
      context.tenantId(),
      "tenant-create",
      idempotencyKey,
      requestHash,
      () -> doCreateTenant(tenantCode, tenantName)
    );
  }

  public TenantInfo getTenant(String tenantId) {
    permissionGuard.check("platform.tenant.read");
    assertTenantInScope(tenantId);
    return repository.findTenant(tenantId)
      .orElseThrow(() -> new NotFoundException("Tenant not found"));
  }

  public TenantInfo updateTenant(String tenantId, Map<String, Object> body) {
    permissionGuard.check("platform.tenant.update");
    assertTenantInScope(tenantId);
    TenantInfo existing = repository.findTenant(tenantId)
      .orElseThrow(() -> new NotFoundException("Tenant not found"));

    String tenantName = asString(body.get("tenantName"));
    String status = asString(body.get("status"));
    TenantInfo updated = new TenantInfo(
      existing.id(),
      existing.tenantCode(),
      StringUtils.hasText(tenantName) ? tenantName : existing.tenantName(),
      StringUtils.hasText(status) ? status : existing.status(),
      existing.createdAt(),
      OffsetDateTime.now()
    );
    repository.saveTenant(updated);

    auditService.append(
      "TENANT_UPDATE",
      "tenant:" + tenantId,
      "SUCCESS",
      Map.of("tenantName", updated.tenantName(), "status", updated.status())
    );

    return updated;
  }

  public List<OrgUnit> listTenantOrgs(String tenantId) {
    permissionGuard.check("platform.org.read");
    assertTenantInScope(tenantId);
    return repository.listOrgs(tenantId);
  }

  public List<OrgUnit> listTenantStores(String tenantId) {
    permissionGuard.check("platform.org.read");
    assertTenantInScope(tenantId);
    return repository.listStoreOrgs(tenantId);
  }

  public OrgUnit createStore(String tenantId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("platform.org.manage");
    assertTenantInScope(tenantId);

    String storeCode = asString(body.get("storeCode")).trim().toUpperCase();
    if (!StringUtils.hasText(storeCode)) {
      storeCode = asString(body.get("orgCode")).trim().toUpperCase();
    }
    String storeName = asString(body.get("storeName")).trim();
    if (!StringUtils.hasText(storeName)) {
      storeName = asString(body.get("orgName")).trim();
    }
    String parentOrgId = asString(body.get("parentOrgId")).trim();

    if (!StringUtils.hasText(storeCode)) {
      throw new BadRequestException("storeCode is required");
    }
    if (!StringUtils.hasText(storeName)) {
      throw new BadRequestException("storeName is required");
    }
    if (!StringUtils.hasText(parentOrgId)) {
      parentOrgId = repository.listOrgs(tenantId).stream()
        .filter(org -> "AGENT".equalsIgnoreCase(org.orgType()))
        .map(OrgUnit::id)
        .findFirst()
        .orElse("");
    }
    if (StringUtils.hasText(parentOrgId) && repository.findOrg(tenantId, parentOrgId).isEmpty()) {
      throw new BadRequestException("parentOrgId not found in tenant scope");
    }

    String requestHash = tenantId + "|" + storeCode + "|" + storeName + "|" + parentOrgId;
    final String finalStoreCode = storeCode;
    final String finalStoreName = storeName;
    final String finalParentOrgId = parentOrgId;
    return (OrgUnit) idempotencyService.execute(
      tenantId,
      "store-create",
      idempotencyKey,
      requestHash,
      () -> doCreateOrg(tenantId, finalStoreCode, finalStoreName, "STORE", finalParentOrgId)
    );
  }

  public OrgUnit getStore(String storeId) {
    permissionGuard.check("platform.org.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.findStoreOrg(tenantId, storeId)
      .orElseThrow(() -> new NotFoundException("Store not found"));
  }

  public OrgUnit updateStore(String storeId, Map<String, Object> body) {
    permissionGuard.check("platform.org.manage");
    RequestContext context = RequestContextHolder.get();
    OrgUnit existing = repository.findStoreOrg(context.tenantId(), storeId)
      .orElseThrow(() -> new NotFoundException("Store not found"));

    String storeName = asString(body.get("storeName")).trim();
    if (!StringUtils.hasText(storeName)) {
      storeName = asString(body.get("orgName")).trim();
    }
    String status = asString(body.get("status")).trim().toUpperCase();

    OrgUnit updated = new OrgUnit(
      existing.id(),
      existing.tenantId(),
      existing.orgCode(),
      StringUtils.hasText(storeName) ? storeName : existing.orgName(),
      existing.orgType(),
      existing.parentOrgId(),
      StringUtils.hasText(status) ? status : existing.status(),
      existing.createdAt(),
      OffsetDateTime.now()
    );
    repository.saveOrg(updated);
    auditService.append(
      "STORE_UPDATE",
      "store:" + storeId,
      "SUCCESS",
      Map.of("storeName", updated.orgName(), "status", updated.status())
    );
    return updated;
  }

  public List<Map<String, Object>> listStoreAgreements(String storeId) {
    permissionGuard.check("platform.org.read");
    String tenantId = RequestContextHolder.get().tenantId();
    repository.findStoreOrg(tenantId, storeId).orElseThrow(() -> new NotFoundException("Store not found"));
    return repository.listStoreAgreements(tenantId, storeId);
  }

  public Map<String, Object> createStoreAgreement(String storeId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("platform.org.manage");
    RequestContext context = RequestContextHolder.get();
    repository.findStoreOrg(context.tenantId(), storeId).orElseThrow(() -> new NotFoundException("Store not found"));

    String agreementCode = asString(body.get("agreementCode")).trim().toUpperCase();
    String name = asString(body.get("name")).trim();
    if (!StringUtils.hasText(agreementCode)) {
      throw new BadRequestException("agreementCode is required");
    }
    if (!StringUtils.hasText(name)) {
      throw new BadRequestException("name is required");
    }
    String effectiveFrom = asString(body.get("effectiveFrom")).trim();
    String effectiveTo = asString(body.get("effectiveTo")).trim();
    String status = asString(body.get("status")).trim().toUpperCase();
    if (!StringUtils.hasText(status)) {
      status = "ACTIVE";
    }
    double revenueShareRate = parseDouble(body.get("revenueShareRate"), 0.35d);

    String requestHash = context.tenantId() + "|" + storeId + "|" + agreementCode + "|" + name + "|" + effectiveFrom + "|" + effectiveTo + "|" + revenueShareRate;
    final String finalStatus = status;
    final String finalEffectiveFrom = effectiveFrom;
    final String finalEffectiveTo = effectiveTo;
    final double finalRevenueShareRate = revenueShareRate;
    @SuppressWarnings("unchecked")
    Map<String, Object> created = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "store-agreement-create:" + storeId,
      idempotencyKey,
      requestHash,
      () -> doCreateStoreAgreement(
        context,
        storeId,
        agreementCode,
        name,
        finalStatus,
        finalEffectiveFrom,
        finalEffectiveTo,
        finalRevenueShareRate
      )
    );
    return created;
  }

  public OrgUnit createOrg(String tenantId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("platform.org.manage");
    assertTenantInScope(tenantId);

    String orgCode = asString(body.get("orgCode")).trim().toUpperCase();
    String orgName = asString(body.get("orgName")).trim();
    String orgType = asString(body.get("orgType")).trim().toUpperCase();
    String parentOrgId = asString(body.get("parentOrgId")).trim();

    if (!StringUtils.hasText(orgCode)) {
      throw new BadRequestException("orgCode is required");
    }
    if (!StringUtils.hasText(orgName)) {
      throw new BadRequestException("orgName is required");
    }
    if (!StringUtils.hasText(orgType)) {
      orgType = "STORE";
    }
    if (StringUtils.hasText(parentOrgId) && repository.findOrg(tenantId, parentOrgId).isEmpty()) {
      throw new BadRequestException("parentOrgId not found in tenant scope");
    }

    String requestHash = tenantId + "|" + orgCode + "|" + orgName + "|" + orgType + "|" + parentOrgId;
    final String finalOrgType = orgType;
    final String finalParentOrgId = parentOrgId;
    return (OrgUnit) idempotencyService.execute(
      tenantId,
      "org-create",
      idempotencyKey,
      requestHash,
      () -> doCreateOrg(tenantId, orgCode, orgName, finalOrgType, finalParentOrgId)
    );
  }

  public List<Map<String, Object>> orgTree() {
    permissionGuard.check("platform.org.read");
    String tenantId = RequestContextHolder.get().tenantId();
    List<OrgUnit> orgs = repository.listOrgs(tenantId);

    Map<String, List<OrgUnit>> childrenByParent = new HashMap<>();
    for (OrgUnit org : orgs) {
      String parent = StringUtils.hasText(org.parentOrgId()) ? org.parentOrgId() : "__ROOT__";
      childrenByParent.computeIfAbsent(parent, ignore -> new ArrayList<>()).add(org);
    }

    for (List<OrgUnit> childList : childrenByParent.values()) {
      childList.sort(Comparator.comparing(OrgUnit::orgCode));
    }

    return buildTree(childrenByParent, "__ROOT__");
  }

  private TenantInfo doCreateTenant(String tenantCode, String tenantName) {
    boolean exists = repository.listTenants(1, Integer.MAX_VALUE).stream()
      .anyMatch(tenant -> tenant.tenantCode().equalsIgnoreCase(tenantCode));
    if (exists) {
      throw new ConflictException("tenantCode already exists");
    }

    OffsetDateTime now = OffsetDateTime.now();
    String tenantId = "tenant-" + tenantCode.toLowerCase();
    TenantInfo tenant = new TenantInfo(tenantId, tenantCode, tenantName, "ACTIVE", now, now);
    repository.saveTenant(tenant);

    OrgUnit rootOrg = new OrgUnit(
      UUID.randomUUID().toString(),
      tenantId,
      "ROOT",
      tenantName + "-ROOT",
      "PLATFORM",
      "",
      "ACTIVE",
      now,
      now
    );
    repository.saveOrg(rootOrg);

    auditService.append(
      "TENANT_CREATE",
      "tenant:" + tenantId,
      "SUCCESS",
      Map.of("tenantCode", tenantCode, "tenantName", tenantName)
    );

    return tenant;
  }

  private OrgUnit doCreateOrg(String tenantId, String orgCode, String orgName, String orgType, String parentOrgId) {
    boolean exists = repository.listOrgs(tenantId).stream()
      .anyMatch(org -> org.orgCode().equalsIgnoreCase(orgCode));
    if (exists) {
      throw new ConflictException("orgCode already exists in tenant");
    }

    OffsetDateTime now = OffsetDateTime.now();
    OrgUnit org = new OrgUnit(
      UUID.randomUUID().toString(),
      tenantId,
      orgCode,
      orgName,
      orgType,
      parentOrgId,
      "ACTIVE",
      now,
      now
    );
    repository.saveOrg(org);

    auditService.append(
      "ORG_CREATE",
      "org:" + org.id(),
      "SUCCESS",
      Map.of("tenantId", tenantId, "orgCode", orgCode, "orgType", orgType, "parentOrgId", parentOrgId)
    );

    return org;
  }

  private List<Map<String, Object>> buildTree(Map<String, List<OrgUnit>> childrenByParent, String parentId) {
    List<OrgUnit> children = childrenByParent.getOrDefault(parentId, List.of());
    List<Map<String, Object>> nodes = new ArrayList<>();
    for (OrgUnit org : children) {
      nodes.add(Map.of(
        "id", org.id(),
        "orgCode", org.orgCode(),
        "orgName", org.orgName(),
        "orgType", org.orgType(),
        "status", org.status(),
        "children", buildTree(childrenByParent, org.id())
      ));
    }
    return nodes;
  }

  private Map<String, Object> doCreateStoreAgreement(
    RequestContext context,
    String storeId,
    String agreementCode,
    String name,
    String status,
    String effectiveFrom,
    String effectiveTo,
    double revenueShareRate
  ) {
    boolean codeExists = repository.listStoreAgreements(context.tenantId(), storeId).stream()
      .anyMatch(row -> agreementCode.equalsIgnoreCase(asString(row.get("agreementCode"))));
    if (codeExists) {
      throw new ConflictException("agreementCode already exists in store scope");
    }

    String agreementId = UUID.randomUUID().toString();
    Map<String, Object> agreement = new HashMap<>();
    agreement.put("agreementId", agreementId);
    agreement.put("tenantId", context.tenantId());
    agreement.put("storeId", storeId);
    agreement.put("agreementCode", agreementCode);
    agreement.put("name", name);
    agreement.put("status", status);
    agreement.put("effectiveFrom", effectiveFrom);
    agreement.put("effectiveTo", effectiveTo);
    agreement.put("revenueShareRate", revenueShareRate);
    agreement.put("updatedAt", OffsetDateTime.now().toString());
    agreement.put("requestId", context.requestId());

    repository.appendStoreAgreement(context.tenantId(), storeId, agreement);
    auditService.append(
      "STORE_AGREEMENT_CREATE",
      "store-agreement:" + agreementId,
      "SUCCESS",
      Map.of(
        "storeId", storeId,
        "agreementCode", agreementCode,
        "revenueShareRate", revenueShareRate
      )
    );
    return agreement;
  }

  private double parseDouble(Object raw, double defaultValue) {
    if (raw == null) {
      return defaultValue;
    }
    if (raw instanceof Number number) {
      return number.doubleValue();
    }
    String text = asString(raw).trim();
    if (!StringUtils.hasText(text)) {
      return defaultValue;
    }
    try {
      return Double.parseDouble(text);
    } catch (NumberFormatException ex) {
      throw new BadRequestException("invalid decimal value: " + text);
    }
  }

  private void assertTenantInScope(String tenantId) {
    RequestContext context = RequestContextHolder.get();
    if (context.hasPermission("platform.tenant.cross.read")) {
      return;
    }
    if (!context.tenantId().equals(tenantId)) {
      throw new NotFoundException("Tenant not found");
    }
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
