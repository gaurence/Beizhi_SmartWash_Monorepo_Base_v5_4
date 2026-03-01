package com.beizhi.smartwash.infrastructure.repository;

import com.beizhi.smartwash.domain.model.AuditLog;
import com.beizhi.smartwash.domain.model.AuditExportJob;
import com.beizhi.smartwash.domain.model.AbacPolicy;
import com.beizhi.smartwash.domain.model.Cabinet;
import com.beizhi.smartwash.domain.model.CabinetStoreBinding;
import com.beizhi.smartwash.domain.model.DeviceInvestor;
import com.beizhi.smartwash.domain.model.AccessGrant;
import com.beizhi.smartwash.domain.model.IamPermission;
import com.beizhi.smartwash.domain.model.IamRole;
import com.beizhi.smartwash.domain.model.IamUser;
import com.beizhi.smartwash.domain.model.IdempotencyRecord;
import com.beizhi.smartwash.domain.model.OrgUnit;
import com.beizhi.smartwash.domain.model.PayoutAccount;
import com.beizhi.smartwash.domain.model.RefundRequestRecord;
import com.beizhi.smartwash.domain.model.Site;
import com.beizhi.smartwash.domain.model.SiteQueueItem;
import com.beizhi.smartwash.domain.model.Slot;
import com.beizhi.smartwash.domain.model.SlotState;
import com.beizhi.smartwash.domain.model.TenantInfo;
import com.beizhi.smartwash.domain.model.TicketRecord;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Repository
public class InMemoryPlatformRepository {
  private final Map<String, TenantInfo> tenants = new ConcurrentHashMap<>();
  private final Map<String, Map<String, OrgUnit>> orgsByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, IamUser>> usersByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, IamRole>> rolesByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, IamPermission>> permissionsByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, AbacPolicy>> policiesByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, AccessGrant>> accessGrantsByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, AuditExportJob>> auditExportsByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Cabinet>> cabinetsByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Site>> sitesByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Slot>> slotsByTenantAndKey = new ConcurrentHashMap<>();
  private final Map<String, Map<String, List<SiteQueueItem>>> queuesByTenantAndSite = new ConcurrentHashMap<>();
  private final Map<String, Map<String, CabinetStoreBinding>> bindingsByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, DeviceInvestor>> investorsByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, List<String>>> investorCabinetIdsByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, PayoutAccount>> payoutAccountsByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, List<Map<String, Object>>>> storeAgreementsByTenantAndStore = new ConcurrentHashMap<>();
  private final Map<String, Map<String, TicketRecord>> ticketsByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, RefundRequestRecord>> refundsByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Map<String, Object>>> settlementsByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, List<Map<String, Object>>>> settlementLinesByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Map<String, Object>>> reportSnapshotsByTenantAndView = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Map<String, Object>>> reportExportsByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Map<String, Object>>> staffByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Map<String, Object>>> appOrdersByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, List<Map<String, Object>>>> appOrderTimelineByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, List<Map<String, Object>>>> appOrderEvidenceByTenant = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Map<String, Object>>> appTechTasksByTenant = new ConcurrentHashMap<>();
  private final List<AuditLog> auditLogs = new CopyOnWriteArrayList<>();
  private final Map<String, IdempotencyRecord> idempotencyRecords = new ConcurrentHashMap<>();
  private final Map<String, ReentrantLock> slotLocks = new ConcurrentHashMap<>();
  private final Map<String, List<Map<String, Object>>> eventsByTenantAndCabinet = new ConcurrentHashMap<>();

  @PostConstruct
  public void seed() {
    seedTenantIdentity("tenant-a", "TENANT_A", "华南智洗租户");
    seedTenantIdentity("tenant-b", "TENANT_B", "云谷智洗租户");

    Cabinet tenantACabinet = new Cabinet("CAB-A1", "tenant-a", "SITE-A", "SER-A1", "ONLINE");
    Cabinet tenantBCabinet = new Cabinet("CAB-B1", "tenant-b", "SITE-B", "SER-B1", "ONLINE");

    cabinetsByTenant.computeIfAbsent("tenant-a", ignore -> new ConcurrentHashMap<>())
      .put(tenantACabinet.id(), tenantACabinet);
    cabinetsByTenant.computeIfAbsent("tenant-b", ignore -> new ConcurrentHashMap<>())
      .put(tenantBCabinet.id(), tenantBCabinet);

    saveSite(new Site("SITE-A", "tenant-a", "星河社区 A 站", "Nanshan", 0.9d, 3, "OPEN"));
    saveSite(new Site("SITE-A2", "tenant-a", "星河社区 B 站", "Nanshan", 2.6d, 1, "OPEN"));
    saveSite(new Site("SITE-B", "tenant-b", "云谷园区 1 站", "Baoan", 1.2d, 2, "OPEN"));
    saveSite(new Site("SITE-B2", "tenant-b", "云谷园区 2 站", "Baoan", 4.8d, 0, "MAINTENANCE"));

    seedSiteQueue("tenant-a", "SITE-A", List.of(
      new SiteQueueItem("Q-2026-0001", "STANDARD", 12, "WAITING"),
      new SiteQueueItem("Q-2026-0002", "PREMIUM", 18, "WAITING"),
      new SiteQueueItem("Q-2026-0003", "STANDARD", 27, "PROCESSING")
    ));
    seedSiteQueue("tenant-a", "SITE-A2", List.of(
      new SiteQueueItem("Q-2026-0010", "STANDARD", 8, "WAITING")
    ));
    seedSiteQueue("tenant-b", "SITE-B", List.of(
      new SiteQueueItem("Q-2026-1001", "STANDARD", 14, "WAITING"),
      new SiteQueueItem("Q-2026-1002", "STANDARD", 21, "WAITING")
    ));
    seedSiteQueue("tenant-b", "SITE-B2", List.of());
    seedAppOrders("tenant-a", "customer-0000");
    seedAppOrders("tenant-b", "customer-0001");
    seedAppTechTasks("tenant-a");
    seedAppTechTasks("tenant-b");

    saveSlot(new Slot(UUID.randomUUID().toString(), "tenant-a", "CAB-A1", "S1", SlotState.RESERVED));
    saveSlot(new Slot(UUID.randomUUID().toString(), "tenant-a", "CAB-A1", "S2", SlotState.OCCUPIED));
    saveSlot(new Slot(UUID.randomUUID().toString(), "tenant-a", "CAB-A1", "S3", SlotState.LOCKED));

    saveSlot(new Slot(UUID.randomUUID().toString(), "tenant-b", "CAB-B1", "S1", SlotState.RESERVED));

    CabinetStoreBinding binding = new CabinetStoreBinding(
      UUID.randomUUID().toString(),
      "tenant-a",
      "CAB-A1",
      "STORE-A",
      "SHARED",
      "PENDING"
    );
    saveBinding(binding);

    saveInvestor(new DeviceInvestor("INV-A1", "tenant-a", "华南设备合伙人", "THIRD_PARTY", "ACTIVE"), List.of("CAB-A1"));
    saveInvestor(new DeviceInvestor("INV-B1", "tenant-b", "云谷设备投资中心", "AGENT", "ACTIVE"), List.of("CAB-B1"));

    savePayoutAccount(new PayoutAccount(
      "PA-TENANT-A-001",
      "tenant-a",
      "ORG-TENANT-A-ROOT",
      "BANK",
      "62****1001",
      "enc:tenant-a:001",
      "华南设备合伙人",
      "MCH-A-001",
      "ACTIVE"
    ));
    savePayoutAccount(new PayoutAccount(
      "PA-TENANT-B-001",
      "tenant-b",
      "ORG-TENANT-B-ROOT",
      "BANK",
      "62****2001",
      "enc:tenant-b:001",
      "云谷设备投资中心",
      "MCH-B-001",
      "ACTIVE"
    ));

    seedSettlements("tenant-a", "INV-A1");
    seedSettlements("tenant-b", "INV-B1");
    seedTickets("tenant-a");
    seedTickets("tenant-b");
    seedRefunds("tenant-a");
    seedRefunds("tenant-b");

    appendEvent("tenant-a", "CAB-A1", "HEARTBEAT", Map.of("online", true, "occurredAt", OffsetDateTime.now().toString()));
  }

  public List<TenantInfo> listTenants(int page, int size) {
    List<TenantInfo> all = new ArrayList<>(tenants.values());
    all.sort(Comparator.comparing(TenantInfo::id));
    return slice(all, page, size);
  }

  public long countTenants() {
    return tenants.size();
  }

  public Optional<TenantInfo> findTenant(String tenantId) {
    return Optional.ofNullable(tenants.get(tenantId));
  }

  public void saveTenant(TenantInfo tenant) {
    tenants.put(tenant.id(), tenant);
  }

  public List<OrgUnit> listOrgs(String tenantId) {
    List<OrgUnit> all = new ArrayList<>(orgsByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort(Comparator.comparing(OrgUnit::orgCode));
    return all;
  }

  public List<OrgUnit> listStoreOrgs(String tenantId) {
    return listOrgs(tenantId).stream()
      .filter(org -> "STORE".equalsIgnoreCase(org.orgType()))
      .toList();
  }

  public Optional<OrgUnit> findOrg(String tenantId, String orgId) {
    return Optional.ofNullable(orgsByTenant.getOrDefault(tenantId, Map.of()).get(orgId));
  }

  public Optional<OrgUnit> findStoreOrg(String tenantId, String storeId) {
    return findOrg(tenantId, storeId)
      .filter(org -> "STORE".equalsIgnoreCase(org.orgType()));
  }

  public void saveOrg(OrgUnit org) {
    orgsByTenant.computeIfAbsent(org.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(org.id(), org);
  }

  public List<Map<String, Object>> listStoreAgreements(String tenantId, String storeId) {
    List<Map<String, Object>> rows = storeAgreementsByTenantAndStore
      .getOrDefault(tenantId, Map.of())
      .getOrDefault(storeId, List.of());
    return rows.stream()
      .sorted((left, right) -> asString(right.get("effectiveFrom")).compareTo(asString(left.get("effectiveFrom"))))
      .toList();
  }

  public void appendStoreAgreement(String tenantId, String storeId, Map<String, Object> agreement) {
    Map<String, Object> row = new LinkedHashMap<>(agreement);
    String agreementId = asString(row.get("agreementId"));
    if (agreementId.isBlank()) {
      agreementId = UUID.randomUUID().toString();
    }
    row.put("agreementId", agreementId);
    row.put("storeId", storeId);
    row.putIfAbsent("createdAt", OffsetDateTime.now().toString());
    storeAgreementsByTenantAndStore
      .computeIfAbsent(tenantId, ignore -> new ConcurrentHashMap<>())
      .computeIfAbsent(storeId, ignore -> new CopyOnWriteArrayList<>())
      .add(Map.copyOf(row));
  }

  public Optional<Map<String, Object>> findReportSnapshot(String tenantId, String viewScope) {
    return Optional.ofNullable(
      reportSnapshotsByTenantAndView
        .getOrDefault(tenantId, Map.of())
        .getOrDefault(viewScope, null)
    );
  }

  public void saveReportSnapshot(String tenantId, String viewScope, Map<String, Object> snapshot) {
    reportSnapshotsByTenantAndView
      .computeIfAbsent(tenantId, ignore -> new ConcurrentHashMap<>())
      .put(viewScope, Map.copyOf(snapshot));
  }

  public void saveReportExport(String tenantId, Map<String, Object> exportJob) {
    String exportId = asString(exportJob.get("exportId"));
    if (exportId.isBlank()) {
      throw new IllegalArgumentException("exportId is required");
    }
    reportExportsByTenant
      .computeIfAbsent(tenantId, ignore -> new ConcurrentHashMap<>())
      .put(exportId, Map.copyOf(exportJob));
  }

  public List<IamUser> listUsers(String tenantId, int page, int size) {
    List<IamUser> all = new ArrayList<>(usersByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort(Comparator.comparing(IamUser::username));
    return slice(all, page, size);
  }

  public long countUsers(String tenantId) {
    return usersByTenant.getOrDefault(tenantId, Map.of()).size();
  }

  public Optional<IamUser> findUser(String tenantId, String userId) {
    return Optional.ofNullable(usersByTenant.getOrDefault(tenantId, Map.of()).get(userId));
  }

  public void saveUser(IamUser user) {
    usersByTenant.computeIfAbsent(user.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(user.id(), user);
  }

  public List<IamRole> listRoles(String tenantId, int page, int size) {
    List<IamRole> all = new ArrayList<>(rolesByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort(Comparator.comparing(IamRole::roleCode));
    return slice(all, page, size);
  }

  public long countRoles(String tenantId) {
    return rolesByTenant.getOrDefault(tenantId, Map.of()).size();
  }

  public Optional<IamRole> findRole(String tenantId, String roleId) {
    return Optional.ofNullable(rolesByTenant.getOrDefault(tenantId, Map.of()).get(roleId));
  }

  public void saveRole(IamRole role) {
    rolesByTenant.computeIfAbsent(role.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(role.id(), role);
  }

  public List<IamPermission> listPermissions(String tenantId) {
    List<IamPermission> all = new ArrayList<>(permissionsByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort(Comparator.comparing(IamPermission::permissionKey));
    return all;
  }

  public Optional<IamPermission> findPermissionByKey(String tenantId, String permissionKey) {
    return permissionsByTenant.getOrDefault(tenantId, Map.of()).values().stream()
      .filter(permission -> permission.permissionKey().equals(permissionKey))
      .findFirst();
  }

  public void savePermission(IamPermission permission) {
    permissionsByTenant.computeIfAbsent(permission.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(permission.id(), permission);
  }

  public List<AbacPolicy> listAbacPolicies(String tenantId, int page, int size) {
    List<AbacPolicy> all = new ArrayList<>(policiesByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort(Comparator.comparing(AbacPolicy::policyCode));
    return slice(all, page, size);
  }

  public long countAbacPolicies(String tenantId) {
    return policiesByTenant.getOrDefault(tenantId, Map.of()).size();
  }

  public Optional<AbacPolicy> findAbacPolicy(String tenantId, String policyId) {
    return Optional.ofNullable(policiesByTenant.getOrDefault(tenantId, Map.of()).get(policyId));
  }

  public void saveAbacPolicy(AbacPolicy policy) {
    policiesByTenant.computeIfAbsent(policy.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(policy.id(), policy);
  }

  public List<AccessGrant> listAccessGrants(String tenantId, int page, int size) {
    List<AccessGrant> all = new ArrayList<>(accessGrantsByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort(Comparator.comparing(AccessGrant::issuedAt).reversed());
    return slice(all, page, size);
  }

  public long countAccessGrants(String tenantId) {
    return accessGrantsByTenant.getOrDefault(tenantId, Map.of()).size();
  }

  public Optional<AccessGrant> findAccessGrant(String tenantId, String grantId) {
    return Optional.ofNullable(accessGrantsByTenant.getOrDefault(tenantId, Map.of()).get(grantId));
  }

  public void saveAccessGrant(AccessGrant accessGrant) {
    accessGrantsByTenant.computeIfAbsent(accessGrant.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(accessGrant.id(), accessGrant);
  }

  public List<AuditExportJob> listAuditExports(String tenantId, int page, int size) {
    List<AuditExportJob> all = new ArrayList<>(auditExportsByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort(Comparator.comparing(AuditExportJob::createdAt).reversed());
    return slice(all, page, size);
  }

  public long countAuditExports(String tenantId) {
    return auditExportsByTenant.getOrDefault(tenantId, Map.of()).size();
  }

  public void saveAuditExport(AuditExportJob job) {
    auditExportsByTenant.computeIfAbsent(job.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(job.id(), job);
  }

  public List<Site> listSites(String tenantId) {
    return sitesByTenant.getOrDefault(tenantId, Map.of()).values().stream()
      .sorted(Comparator.comparingDouble(Site::distanceKm))
      .toList();
  }

  public Optional<Site> findSite(String tenantId, String siteId) {
    return Optional.ofNullable(sitesByTenant.getOrDefault(tenantId, Map.of()).get(siteId));
  }

  public List<SiteQueueItem> listSiteQueue(String tenantId, String siteId) {
    return queuesByTenantAndSite
      .getOrDefault(tenantId, Map.of())
      .getOrDefault(siteId, List.of());
  }

  public List<Map<String, Object>> listAppOrders(String tenantId, int page, int size) {
    Map<String, Map<String, Object>> tenantOrders = appOrdersByTenant.getOrDefault(
      tenantId,
      Map.<String, Map<String, Object>>of()
    );
    List<Map<String, Object>> all = new ArrayList<>(tenantOrders.values());
    all.sort((left, right) -> asString(right.get("createdAt")).compareTo(asString(left.get("createdAt"))));
    return slice(all, page, size);
  }

  public long countAppOrders(String tenantId) {
    return appOrdersByTenant.getOrDefault(tenantId, Map.<String, Map<String, Object>>of()).size();
  }

  public Optional<Map<String, Object>> findAppOrder(String tenantId, String orderId) {
    return Optional.ofNullable(
      appOrdersByTenant.getOrDefault(tenantId, Map.<String, Map<String, Object>>of()).get(orderId)
    );
  }

  public void saveAppOrder(String tenantId, Map<String, Object> order) {
    String orderId = asString(order.get("orderId"));
    if (orderId.isBlank()) {
      throw new IllegalArgumentException("orderId is required");
    }
    appOrdersByTenant.computeIfAbsent(tenantId, ignore -> new ConcurrentHashMap<>())
      .put(orderId, Map.copyOf(order));
  }

  public List<Map<String, Object>> listAppOrderTimeline(String tenantId, String orderId) {
    return appOrderTimelineByTenant
      .getOrDefault(tenantId, Map.<String, List<Map<String, Object>>>of())
      .getOrDefault(orderId, List.of());
  }

  public void appendAppOrderTimeline(
    String tenantId,
    String orderId,
    String status,
    String title,
    String message
  ) {
    Map<String, Object> event = new LinkedHashMap<>();
    event.put("eventId", UUID.randomUUID().toString());
    event.put("orderId", orderId);
    event.put("status", status);
    event.put("title", title);
    event.put("message", message);
    event.put("occurredAt", OffsetDateTime.now().toString());

    appOrderTimelineByTenant
      .computeIfAbsent(tenantId, ignore -> new ConcurrentHashMap<>())
      .computeIfAbsent(orderId, ignore -> new CopyOnWriteArrayList<>())
      .add(Map.copyOf(event));
  }

  public List<Map<String, Object>> listAppOrderEvidence(String tenantId, String orderId) {
    return appOrderEvidenceByTenant
      .getOrDefault(tenantId, Map.<String, List<Map<String, Object>>>of())
      .getOrDefault(orderId, List.of());
  }

  public void appendAppOrderEvidence(String tenantId, String orderId, Map<String, Object> evidence) {
    OffsetDateTime now = OffsetDateTime.now();
    Map<String, Object> record = new LinkedHashMap<>(evidence);
    String evidenceId = asString(record.get("evidenceId"));
    if (evidenceId.isBlank()) {
      evidenceId = UUID.randomUUID().toString();
    }
    record.put("evidenceId", evidenceId);
    record.put("orderId", orderId);
    record.putIfAbsent("capturedAt", now.toString());
    record.putIfAbsent("createdAt", now.toString());

    appOrderEvidenceByTenant
      .computeIfAbsent(tenantId, ignore -> new ConcurrentHashMap<>())
      .computeIfAbsent(orderId, ignore -> new CopyOnWriteArrayList<>())
      .add(Map.copyOf(record));
  }

  public List<Map<String, Object>> listAppTechTasks(String tenantId, String assigneeUserId, String status, int page, int size) {
    List<Map<String, Object>> all = new ArrayList<>(appTechTasksByTenant.getOrDefault(
      tenantId,
      Map.<String, Map<String, Object>>of()
    ).values());
    all.sort((left, right) -> asString(right.get("createdAt")).compareTo(asString(left.get("createdAt"))));
    List<Map<String, Object>> filtered = all.stream()
      .filter(task -> assigneeUserId == null || assigneeUserId.isBlank() || assigneeUserId.equals(asString(task.get("assigneeUserId"))))
      .filter(task -> status == null || status.isBlank() || status.equalsIgnoreCase(asString(task.get("status"))))
      .toList();
    return slice(filtered, page, size);
  }

  public long countAppTechTasks(String tenantId, String assigneeUserId, String status) {
    return appTechTasksByTenant.getOrDefault(tenantId, Map.<String, Map<String, Object>>of()).values().stream()
      .filter(task -> assigneeUserId == null || assigneeUserId.isBlank() || assigneeUserId.equals(asString(task.get("assigneeUserId"))))
      .filter(task -> status == null || status.isBlank() || status.equalsIgnoreCase(asString(task.get("status"))))
      .count();
  }

  public Optional<Map<String, Object>> findAppTechTask(String tenantId, String taskId) {
    return Optional.ofNullable(
      appTechTasksByTenant.getOrDefault(tenantId, Map.<String, Map<String, Object>>of()).get(taskId)
    );
  }

  public void saveAppTechTask(String tenantId, Map<String, Object> task) {
    String taskId = asString(task.get("taskId"));
    if (taskId.isBlank()) {
      throw new IllegalArgumentException("taskId is required");
    }
    appTechTasksByTenant.computeIfAbsent(tenantId, ignore -> new ConcurrentHashMap<>())
      .put(taskId, Map.copyOf(task));
  }

  public List<Cabinet> listCabinets(String tenantId, int page, int size) {
    List<Cabinet> all = new ArrayList<>(cabinetsByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort(Comparator.comparing(Cabinet::id));
    return slice(all, page, size);
  }

  public long countCabinets(String tenantId) {
    return cabinetsByTenant.getOrDefault(tenantId, Map.of()).size();
  }

  public Optional<Cabinet> findCabinet(String tenantId, String cabinetId) {
    return Optional.ofNullable(cabinetsByTenant.getOrDefault(tenantId, Map.of()).get(cabinetId));
  }

  public List<DeviceInvestor> listDeviceInvestors(String tenantId, int page, int size) {
    List<DeviceInvestor> all = new ArrayList<>(investorsByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort(Comparator.comparing(DeviceInvestor::id));
    return slice(all, page, size);
  }

  public long countDeviceInvestors(String tenantId) {
    return investorsByTenant.getOrDefault(tenantId, Map.of()).size();
  }

  public Optional<DeviceInvestor> findDeviceInvestor(String tenantId, String investorId) {
    return Optional.ofNullable(investorsByTenant.getOrDefault(tenantId, Map.of()).get(investorId));
  }

  public List<Cabinet> listInvestorCabinets(String tenantId, String investorId) {
    List<String> cabinetIds = investorCabinetIdsByTenant
      .getOrDefault(tenantId, Map.of())
      .getOrDefault(investorId, List.of());

    return cabinetIds.stream()
      .map(id -> cabinetsByTenant.getOrDefault(tenantId, Map.of()).get(id))
      .filter(cabinet -> cabinet != null)
      .toList();
  }

  public List<PayoutAccount> listPayoutAccounts(String tenantId, int page, int size) {
    List<PayoutAccount> all = new ArrayList<>(payoutAccountsByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort(Comparator.comparing(PayoutAccount::createdAt).reversed());
    return slice(all, page, size);
  }

  public long countPayoutAccounts(String tenantId) {
    return payoutAccountsByTenant.getOrDefault(tenantId, Map.of()).size();
  }

  public Optional<PayoutAccount> findPayoutAccount(String tenantId, String accountId) {
    return Optional.ofNullable(payoutAccountsByTenant.getOrDefault(tenantId, Map.of()).get(accountId));
  }

  public void savePayoutAccount(PayoutAccount payoutAccount) {
    payoutAccountsByTenant.computeIfAbsent(payoutAccount.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(payoutAccount.id(), payoutAccount);
  }

  public List<RefundRequestRecord> listRefunds(String tenantId, int page, int size) {
    List<RefundRequestRecord> all = new ArrayList<>(refundsByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort(Comparator.comparing(RefundRequestRecord::createdAt).reversed());
    return slice(all, page, size);
  }

  public List<TicketRecord> listTickets(String tenantId, int page, int size) {
    List<TicketRecord> all = new ArrayList<>(ticketsByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort(Comparator.comparing(TicketRecord::createdAt).reversed());
    return slice(all, page, size);
  }

  public long countTickets(String tenantId) {
    return ticketsByTenant.getOrDefault(tenantId, Map.of()).size();
  }

  public Optional<TicketRecord> findTicket(String tenantId, String ticketId) {
    return Optional.ofNullable(ticketsByTenant.getOrDefault(tenantId, Map.of()).get(ticketId));
  }

  public void saveTicket(TicketRecord ticket) {
    ticketsByTenant.computeIfAbsent(ticket.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(ticket.ticketId(), ticket);
  }

  public long countRefunds(String tenantId) {
    return refundsByTenant.getOrDefault(tenantId, Map.of()).size();
  }

  public Optional<RefundRequestRecord> findRefund(String tenantId, String refundId) {
    return Optional.ofNullable(refundsByTenant.getOrDefault(tenantId, Map.of()).get(refundId));
  }

  public void saveRefund(RefundRequestRecord refund) {
    refundsByTenant.computeIfAbsent(refund.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(refund.refundId(), refund);
  }

  public List<Map<String, Object>> listSettlements(String tenantId, int page, int size) {
    List<Map<String, Object>> all = new ArrayList<>(settlementsByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort((left, right) -> asString(right.get("periodEnd")).compareTo(asString(left.get("periodEnd"))));
    return slice(all, page, size);
  }

  public long countSettlements(String tenantId) {
    return settlementsByTenant.getOrDefault(tenantId, Map.of()).size();
  }

  public Optional<Map<String, Object>> findSettlement(String tenantId, String settlementId) {
    return Optional.ofNullable(settlementsByTenant.getOrDefault(tenantId, Map.of()).get(settlementId));
  }

  public List<Map<String, Object>> listSettlementLines(String tenantId, String settlementId, int page, int size) {
    List<Map<String, Object>> all = new ArrayList<>(settlementLinesByTenant
      .getOrDefault(tenantId, Map.of())
      .getOrDefault(settlementId, List.of()));
    all.sort(Comparator.comparing(item -> asString(item.get("lineId"))));
    return slice(all, page, size);
  }

  public long countSettlementLines(String tenantId, String settlementId) {
    return settlementLinesByTenant
      .getOrDefault(tenantId, Map.of())
      .getOrDefault(settlementId, List.of())
      .size();
  }

  public List<Map<String, Object>> listStaff(String tenantId, String staffType, int page, int size) {
    List<Map<String, Object>> all = new ArrayList<>(staffByTenant.getOrDefault(tenantId, Map.of()).values());
    all.sort(Comparator.comparing(item -> asString(item.get("employeeCode"))));
    List<Map<String, Object>> filtered = all.stream()
      .filter(item -> matchesStaffType(item, staffType))
      .toList();
    return slice(filtered, page, size);
  }

  public long countStaff(String tenantId, String staffType) {
    return staffByTenant.getOrDefault(tenantId, Map.of()).values().stream()
      .filter(item -> matchesStaffType(item, staffType))
      .count();
  }

  public Optional<Map<String, Object>> findStaff(String tenantId, String staffId) {
    return Optional.ofNullable(staffByTenant.getOrDefault(tenantId, Map.of()).get(staffId));
  }

  public void saveStaff(String tenantId, Map<String, Object> staff) {
    String staffId = asString(staff.get("staffId"));
    if (staffId.isBlank()) {
      throw new IllegalArgumentException("staffId is required");
    }
    staffByTenant.computeIfAbsent(tenantId, ignore -> new ConcurrentHashMap<>())
      .put(staffId, Map.copyOf(staff));
  }

  public List<Slot> listSlots(String tenantId, String cabinetId) {
    List<Slot> slots = new ArrayList<>(slotsByTenantAndKey.getOrDefault(tenantId, Map.of()).values());
    return slots.stream()
      .filter(slot -> slot.cabinetId().equals(cabinetId))
      .sorted(Comparator.comparing(Slot::slotNo))
      .toList();
  }

  public Optional<Slot> findSlot(String tenantId, String cabinetId, String slotNo) {
    String key = cabinetId + "::" + slotNo;
    return Optional.ofNullable(slotsByTenantAndKey.getOrDefault(tenantId, Map.of()).get(key));
  }

  public void saveSlot(Slot slot) {
    String key = slot.cabinetId() + "::" + slot.slotNo();
    slotsByTenantAndKey.computeIfAbsent(slot.tenantId(), ignore -> new ConcurrentHashMap<>()).put(key, slot);
  }

  public <T> T withSlotLock(String tenantId, String cabinetId, String slotNo, Supplier<T> supplier) {
    String lockKey = tenantId + "::" + cabinetId + "::" + slotNo;
    ReentrantLock lock = slotLocks.computeIfAbsent(lockKey, ignore -> new ReentrantLock());
    lock.lock();
    try {
      return supplier.get();
    } finally {
      lock.unlock();
    }
  }

  public List<CabinetStoreBinding> listBindings(String tenantId, String cabinetId) {
    return bindingsByTenant.getOrDefault(tenantId, Map.of()).values().stream()
      .filter(binding -> binding.cabinetId().equals(cabinetId))
      .sorted(Comparator.comparing(CabinetStoreBinding::createdAt))
      .toList();
  }

  public Optional<CabinetStoreBinding> findBinding(String tenantId, String bindingId) {
    return Optional.ofNullable(bindingsByTenant.getOrDefault(tenantId, Map.of()).get(bindingId));
  }

  public void saveBinding(CabinetStoreBinding binding) {
    bindingsByTenant.computeIfAbsent(binding.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(binding.id(), binding);
  }

  public void appendAudit(AuditLog log) {
    auditLogs.add(log);
  }

  public List<AuditLog> listAudit(String tenantId, int page, int size) {
    List<AuditLog> all = auditLogs.stream()
      .filter(log -> log.tenantId().equals(tenantId))
      .sorted(Comparator.comparing(AuditLog::createdAt).reversed())
      .toList();
    return slice(all, page, size);
  }

  public long countAudit(String tenantId) {
    return auditLogs.stream().filter(log -> log.tenantId().equals(tenantId)).count();
  }

  public Optional<AuditLog> findAudit(String tenantId, String logId) {
    return auditLogs.stream().filter(log -> log.tenantId().equals(tenantId) && log.id().equals(logId)).findFirst();
  }

  public Optional<IdempotencyRecord> findIdempotency(String key) {
    return Optional.ofNullable(idempotencyRecords.get(key));
  }

  public void saveIdempotency(IdempotencyRecord record) {
    idempotencyRecords.put(record.key(), record);
  }

  public List<Map<String, Object>> listEvents(String tenantId, String cabinetId) {
    return eventsByTenantAndCabinet.getOrDefault(tenantId + "::" + cabinetId, List.of());
  }

  public void appendEvent(String tenantId, String cabinetId, String eventType, Map<String, Object> payload) {
    String key = tenantId + "::" + cabinetId;
    eventsByTenantAndCabinet.computeIfAbsent(key, ignore -> new CopyOnWriteArrayList<>())
      .add(Map.of(
        "eventId", UUID.randomUUID().toString(),
        "eventType", eventType,
        "cabinetId", cabinetId,
        "occurredAt", OffsetDateTime.now().toString(),
        "payload", payload
      ));
  }

  private void seedTenantIdentity(String tenantId, String tenantCode, String tenantName) {
    OffsetDateTime now = OffsetDateTime.now();
    saveTenant(new TenantInfo(tenantId, tenantCode, tenantName, "ACTIVE", now, now));

    String rootOrgId = "ORG-" + tenantId.toUpperCase() + "-ROOT";
    saveOrg(new OrgUnit(rootOrgId, tenantId, "ROOT", tenantName, "PLATFORM", "", "ACTIVE", now, now));
    saveOrg(new OrgUnit(
      "ORG-" + tenantId.toUpperCase() + "-AGENT",
      tenantId,
      "AGENT_MAIN",
      "区域代理中心",
      "AGENT",
      rootOrgId,
      "ACTIVE",
      now,
      now
    ));
    saveOrg(new OrgUnit(
      "ORG-" + tenantId.toUpperCase() + "-STORE-1",
      tenantId,
      "STORE_A",
      "南山一号门店",
      "STORE",
      "ORG-" + tenantId.toUpperCase() + "-AGENT",
      "ACTIVE",
      now,
      now
    ));
    saveOrg(new OrgUnit(
      "ORG-" + tenantId.toUpperCase() + "-STORE-2",
      tenantId,
      "STORE_B",
      "福田二号门店",
      "STORE",
      "ORG-" + tenantId.toUpperCase() + "-AGENT",
      "ACTIVE",
      now,
      now
    ));

    appendStoreAgreement(
      tenantId,
      "ORG-" + tenantId.toUpperCase() + "-STORE-1",
      Map.of(
        "agreementId", "AGR-" + tenantId.toUpperCase() + "-STORE-1-2026",
        "agreementCode", "ST-" + tenantId.toUpperCase() + "-2026-A",
        "name", "标准分润协议",
        "status", "ACTIVE",
        "effectiveFrom", "2026-01-01",
        "effectiveTo", "2026-12-31",
        "revenueShareRate", 0.38d
      )
    );
    appendStoreAgreement(
      tenantId,
      "ORG-" + tenantId.toUpperCase() + "-STORE-2",
      Map.of(
        "agreementId", "AGR-" + tenantId.toUpperCase() + "-STORE-2-2026",
        "agreementCode", "ST-" + tenantId.toUpperCase() + "-2026-B",
        "name", "区域联营协议",
        "status", "ACTIVE",
        "effectiveFrom", "2026-01-01",
        "effectiveTo", "2026-12-31",
        "revenueShareRate", 0.35d
      )
    );

    List<IamPermission> permissions = List.of(
      new IamPermission(UUID.randomUUID().toString(), tenantId, "platform.tenant.read", "租户读取", "TENANT"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "platform.tenant.create", "租户创建", "TENANT"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "platform.tenant.update", "租户编辑", "TENANT"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "platform.org.read", "组织读取", "ORG"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "platform.org.manage", "组织维护", "ORG"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "iam.user.read", "用户读取", "USER"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "iam.user.create", "用户创建", "USER"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "iam.user.update", "用户编辑", "USER"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "iam.user.role.bind", "用户角色绑定", "USER"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "iam.role.read", "角色读取", "ROLE"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "iam.role.create", "角色创建", "ROLE"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "iam.role.update", "角色编辑", "ROLE"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "iam.role.perm.bind", "角色权限绑定", "ROLE"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "iam.perm.read", "权限点读取", "PERMISSION"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "iam.abac.read", "ABAC策略读取", "ABAC"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "iam.abac.policy.manage", "ABAC策略维护", "ABAC"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "iam.access-grant.issue", "按单授权发放", "ABAC"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "audit.log.read", "审计日志读取", "AUDIT"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "audit.export", "审计导出", "AUDIT"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "asset.cabinet.read", "柜体读取", "ASSET"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "iot.open-slot.execute", "开柜指令", "IOT"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "asset.binding.manage", "柜店绑定维护", "ASSET"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "asset.investor.read", "投资方读取", "ASSET"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "finance.payout.read", "收款账户读取", "FINANCE"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "finance.payout.manage", "收款账户维护", "FINANCE"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "finance.ticket.read", "工单读取", "FINANCE"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "finance.ticket.manage", "工单维护", "FINANCE"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "finance.refund.read", "退款读取", "FINANCE"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "finance.refund.manage", "退款维护", "FINANCE"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "finance.settlement.read", "结算读取", "FINANCE"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "finance.settlement.export", "结算导出", "FINANCE"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "app.site.read", "站点读取", "APP"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "app.cabinet.scan", "附近钥匙柜扫描", "APP"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "app.order.read", "订单读取", "APP"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "app.order.create", "订单创建", "APP"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "app.order.deposit-key", "存钥匙", "APP"),
      new IamPermission(UUID.randomUUID().toString(), tenantId, "app.order.retrieve-key", "取钥匙", "APP")
    );
    permissions.forEach(this::savePermission);

    Set<String> allPermissionKeys = permissions.stream().map(IamPermission::permissionKey).collect(java.util.stream.Collectors.toSet());
    saveRole(new IamRole(
      "ROLE-" + tenantId.toUpperCase() + "-PLATFORM_ADMIN",
      tenantId,
      "PLATFORM_ADMIN",
      "平台管理员",
      "TENANT",
      "ACTIVE",
      true,
      allPermissionKeys,
      now,
      now
    ));

    saveRole(new IamRole(
      "ROLE-" + tenantId.toUpperCase() + "-PLATFORM_OPS",
      tenantId,
      "PLATFORM_OPS",
      "平台运维",
      "TENANT",
      "ACTIVE",
      true,
      Set.of("platform.tenant.read", "platform.org.read", "iam.user.read", "iam.role.read", "iam.perm.read", "audit.log.read"),
      now,
      now
    ));

    saveRole(new IamRole(
      "ROLE-" + tenantId.toUpperCase() + "-STORE_ADMIN",
      tenantId,
      "STORE_ADMIN",
      "门店管理员",
      "ORG",
      "ACTIVE",
      true,
      Set.of("iam.user.read", "iam.user.update", "iam.user.role.bind"),
      now,
      now
    ));

    saveUser(new IamUser(
      "USER-" + tenantId.toUpperCase() + "-PA_ADMIN",
      tenantId,
      "pa_admin",
      "平台管理员",
      rootOrgId,
      "ACTIVE",
      "138****0000",
      Set.of("ROLE-" + tenantId.toUpperCase() + "-PLATFORM_ADMIN"),
      now,
      now
    ));
    saveUser(new IamUser(
      "USER-" + tenantId.toUpperCase() + "-OPS_ADMIN",
      tenantId,
      "ops_admin",
      "平台运维",
      rootOrgId,
      "ACTIVE",
      "139****0001",
      Set.of("ROLE-" + tenantId.toUpperCase() + "-PLATFORM_OPS"),
      now,
      now
    ));
    saveUser(new IamUser(
      "USER-" + tenantId.toUpperCase() + "-FIN_ADMIN",
      tenantId,
      "fin_admin",
      "平台财务",
      rootOrgId,
      "ACTIVE",
      "136****0002",
      Set.of("ROLE-" + tenantId.toUpperCase() + "-PLATFORM_ADMIN"),
      now,
      now
    ));
    saveUser(new IamUser(
      "USER-" + tenantId.toUpperCase() + "-IOT_ADMIN",
      tenantId,
      "iot_admin",
      "IoT运维",
      rootOrgId,
      "ACTIVE",
      "136****0003",
      Set.of("ROLE-" + tenantId.toUpperCase() + "-PLATFORM_ADMIN"),
      now,
      now
    ));
    saveUser(new IamUser(
      "USER-" + tenantId.toUpperCase() + "-CS_ADMIN",
      tenantId,
      "cs_admin",
      "平台客服",
      rootOrgId,
      "ACTIVE",
      "136****0004",
      Set.of("ROLE-" + tenantId.toUpperCase() + "-PLATFORM_ADMIN"),
      now,
      now
    ));
    saveUser(new IamUser(
      "USER-" + tenantId.toUpperCase() + "-AGENT_ADMIN",
      tenantId,
      "agent_admin",
      "区域代理管理员",
      "ORG-" + tenantId.toUpperCase() + "-AGENT",
      "ACTIVE",
      "136****0005",
      Set.of("ROLE-" + tenantId.toUpperCase() + "-PLATFORM_ADMIN"),
      now,
      now
    ));
    saveUser(new IamUser(
      "USER-" + tenantId.toUpperCase() + "-STORE_ADMIN",
      tenantId,
      "store_admin",
      "门店管理员",
      "ORG-" + tenantId.toUpperCase() + "-STORE-1",
      "ACTIVE",
      "136****0006",
      Set.of("ROLE-" + tenantId.toUpperCase() + "-STORE_ADMIN"),
      now,
      now
    ));
    saveUser(new IamUser(
      "USER-" + tenantId.toUpperCase() + "-INVESTOR_ADMIN",
      tenantId,
      "investor_admin",
      "投资方管理员",
      rootOrgId,
      "ACTIVE",
      "136****0007",
      Set.of("ROLE-" + tenantId.toUpperCase() + "-PLATFORM_OPS"),
      now,
      now
    ));
    saveUser(new IamUser(
      "USER-" + tenantId.toUpperCase() + "-PROPERTY_ADMIN",
      tenantId,
      "property_admin",
      "物业管理员",
      rootOrgId,
      "ACTIVE",
      "136****0008",
      Set.of("ROLE-" + tenantId.toUpperCase() + "-PLATFORM_OPS"),
      now,
      now
    ));
    saveUser(new IamUser(
      "USER-" + tenantId.toUpperCase() + "-TECH01",
      tenantId,
      "tech01",
      "技师01",
      "ORG-" + tenantId.toUpperCase() + "-STORE-1",
      "ACTIVE",
      "136****0009",
      Set.of("ROLE-" + tenantId.toUpperCase() + "-STORE_ADMIN"),
      now,
      now
    ));
    saveUser(new IamUser(
      "USER-" + tenantId.toUpperCase() + "-VALET01",
      tenantId,
      "valet01",
      "代泊01",
      "ORG-" + tenantId.toUpperCase() + "-STORE-1",
      "ACTIVE",
      "136****0010",
      Set.of("ROLE-" + tenantId.toUpperCase() + "-STORE_ADMIN"),
      now,
      now
    ));

    seedStaffRoster(tenantId, now);

    saveAbacPolicy(new AbacPolicy(
      "ABAC-" + tenantId.toUpperCase() + "-OPEN_SLOT",
      tenantId,
      "OPEN_SLOT_STRICT",
      "开柜按单授权",
      "OPEN_SLOT",
      "CABINET_SLOT",
      "order_id required && ttl <= 300s",
      true,
      now,
      now
    ));
    saveAbacPolicy(new AbacPolicy(
      "ABAC-" + tenantId.toUpperCase() + "-PICKUP_KEY",
      tenantId,
      "PICKUP_KEY_STRICT",
      "取钥匙按单授权",
      "PICKUP_KEY",
      "CABINET_SLOT",
      "order_id required && actor must match task assignee",
      true,
      now,
      now
    ));
  }

  private void saveSite(Site site) {
    sitesByTenant.computeIfAbsent(site.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(site.id(), site);
  }

  private void seedSiteQueue(String tenantId, String siteId, List<SiteQueueItem> queueItems) {
    queuesByTenantAndSite.computeIfAbsent(tenantId, ignore -> new ConcurrentHashMap<>())
      .put(siteId, new CopyOnWriteArrayList<>(queueItems));
  }

  private void seedAppOrders(String tenantId, String customerId) {
    OffsetDateTime now = OffsetDateTime.now();
    String orderId = "ORD-" + tenantId.toUpperCase() + "-0001";

    saveAppOrder(tenantId, createAppOrderRecord(
      orderId,
      tenantId,
      customerId,
      "STANDARD_CARE",
      "Standard Care",
      List.of("ADDON_INTERIOR"),
      "WELCOME10",
      129d,
      10d,
      119d,
      now.minusMinutes(12)
    ));

    appendAppOrderTimeline(
      tenantId,
      orderId,
      "ORDER_CREATED",
      "Order Created",
      "Price snapshot has been recorded"
    );
    appendAppOrderTimeline(
      tenantId,
      orderId,
      "WAITING_DEPOSIT",
      "Waiting for key deposit",
      "Please deposit the key before cutoff"
    );

    appendAppOrderEvidence(
      tenantId,
      orderId,
      Map.of(
        "evidenceId", "EVD-" + orderId + "-001",
        "stage", "SERVICE_START",
        "mediaType", "IMAGE",
        "objectKey", "evidence/" + tenantId + "/" + orderId + "/start-001.jpg",
        "sha256", "sha256:start-001",
        "uploaderUserId", "tech01",
        "uploaderRole", "TECHNICIAN",
        "requestId", "seed-evidence-start-" + orderId.toLowerCase()
      )
    );
    appendAppOrderEvidence(
      tenantId,
      orderId,
      Map.of(
        "evidenceId", "EVD-" + orderId + "-002",
        "stage", "PARKING",
        "mediaType", "IMAGE",
        "objectKey", "evidence/" + tenantId + "/" + orderId + "/parking-001.jpg",
        "sha256", "sha256:parking-001",
        "uploaderUserId", "tech01",
        "uploaderRole", "TECHNICIAN",
        "requestId", "seed-evidence-parking-" + orderId.toLowerCase()
      )
    );
  }

  private void seedAppTechTasks(String tenantId) {
    OffsetDateTime now = OffsetDateTime.now();
    String upperTenant = tenantId.toUpperCase();

    saveAppTechTask(tenantId, createAppTechTaskRecord(
      "TASK-" + upperTenant + "-0001",
      tenantId,
      "ORD-" + upperTenant + "-0001",
      "预检+开工",
      "DISPATCHED",
      "tech01",
      List.of("valet01"),
      now.minusMinutes(10)
    ));
    saveAppTechTask(tenantId, createAppTechTaskRecord(
      "TASK-" + upperTenant + "-0002",
      tenantId,
      "ORD-" + upperTenant + "-0002",
      "取钥匙",
      "ACCEPTED",
      "tech01",
      List.of(),
      now.minusMinutes(35)
    ));
  }

  private Map<String, Object> createAppTechTaskRecord(
    String taskId,
    String tenantId,
    String orderId,
    String title,
    String status,
    String assigneeUserId,
    List<String> collaborators,
    OffsetDateTime createdAt
  ) {
    Map<String, Object> task = new LinkedHashMap<>();
    task.put("taskId", taskId);
    task.put("tenantId", tenantId);
    task.put("orderId", orderId);
    task.put("title", title);
    task.put("status", status);
    task.put("assigneeUserId", assigneeUserId);
    task.put("collaborators", List.copyOf(collaborators));
    task.put("createdAt", createdAt.toString());
    task.put("updatedAt", createdAt.toString());
    task.put("requestId", "seed-" + taskId.toLowerCase());
    return task;
  }

  private Map<String, Object> createAppOrderRecord(
    String orderId,
    String tenantId,
    String customerId,
    String servicePackageId,
    String servicePackageName,
    List<String> addonIds,
    String couponCode,
    double amountSubtotal,
    double amountDiscount,
    double amountTotal,
    OffsetDateTime createdAt
  ) {
    Map<String, Object> order = new LinkedHashMap<>();
    order.put("orderId", orderId);
    order.put("tenantId", tenantId);
    order.put("customerId", customerId);
    order.put("status", "CREATED");
    order.put("fulfillmentMode", "ONSITE");
    order.put("servicePackageId", servicePackageId);
    order.put("servicePackageName", servicePackageName);
    order.put("addonIds", List.copyOf(addonIds));
    order.put("couponCode", couponCode);
    order.put("currency", "CNY");
    order.put("amountSubtotal", amountSubtotal);
    order.put("amountDiscount", amountDiscount);
    order.put("amountTotal", amountTotal);
    order.put("createdAt", createdAt.toString());
    order.put("updatedAt", createdAt.toString());
    order.put("requestId", "seed-" + orderId.toLowerCase());
    return order;
  }

  private void seedSettlements(String tenantId, String investorId) {
    OffsetDateTime now = OffsetDateTime.now();
    String upperTenant = tenantId.toUpperCase();
    String rootOrgId = "ORG-" + upperTenant + "-ROOT";
    String agentOrgId = "ORG-" + upperTenant + "-AGENT";
    String storeOrgId = "ORG-" + upperTenant + "-STORE-1";

    String settlementA = "SET-" + upperTenant + "-202601";
    saveSettlement(tenantId, createSettlementRecord(
      settlementA,
      tenantId,
      "2026-01-01",
      "2026-01-31",
      "COMPLETED",
      24500.0d,
      23180.0d,
      "CNY",
      now.minusDays(20)
    ));
    saveSettlementLines(tenantId, settlementA, List.of(
      createSettlementLineRecord(settlementA, "LINE-001", "PLATFORM", rootOrgId, 6954.0d, "PLATFORM_FEE"),
      createSettlementLineRecord(settlementA, "LINE-002", "AGENT", agentOrgId, 5795.0d, "AGENT_SHARE"),
      createSettlementLineRecord(settlementA, "LINE-003", "STORE", storeOrgId, 9272.0d, "STORE_SHARE"),
      createSettlementLineRecord(settlementA, "LINE-004", "INVESTOR", investorId, 1159.0d, "INVESTOR_SHARE")
    ));

    String settlementB = "SET-" + upperTenant + "-202602";
    saveSettlement(tenantId, createSettlementRecord(
      settlementB,
      tenantId,
      "2026-02-01",
      "2026-02-28",
      "READY_FOR_PAYOUT",
      26800.0d,
      25260.0d,
      "CNY",
      now.minusDays(5)
    ));
    saveSettlementLines(tenantId, settlementB, List.of(
      createSettlementLineRecord(settlementB, "LINE-001", "PLATFORM", rootOrgId, 7578.0d, "PLATFORM_FEE"),
      createSettlementLineRecord(settlementB, "LINE-002", "AGENT", agentOrgId, 6315.0d, "AGENT_SHARE"),
      createSettlementLineRecord(settlementB, "LINE-003", "STORE", storeOrgId, 10104.0d, "STORE_SHARE"),
      createSettlementLineRecord(settlementB, "LINE-004", "INVESTOR", investorId, 1263.0d, "INVESTOR_SHARE")
    ));
  }

  private void seedRefunds(String tenantId) {
    String upperTenant = tenantId.toUpperCase();
    saveRefund(new RefundRequestRecord(
      "REF-" + upperTenant + "-0001",
      tenantId,
      "ORD-" + upperTenant + "-0001",
      39.0d,
      "CNY",
      "SERVICE_DELAY",
      "PENDING_APPROVAL",
      "store_admin",
      "seed-refund-" + tenantId + "-0001"
    ));
    saveRefund(new RefundRequestRecord(
      "REF-" + upperTenant + "-0002",
      tenantId,
      "ORD-" + upperTenant + "-0002",
      15.0d,
      "CNY",
      "RETENTION_FEE_ADJUST",
      "APPROVED",
      "store_admin",
      "seed-refund-" + tenantId + "-0002"
    ));
  }

  private void seedTickets(String tenantId) {
    String upperTenant = tenantId.toUpperCase();
    saveTicket(new TicketRecord(
      "TKT-" + upperTenant + "-0001",
      tenantId,
      "ORD-" + upperTenant + "-0001",
      "CUSTOMER_APP",
      "SERVICE_QUALITY",
      "P1",
      "OPEN",
      "Late delivery complaint",
      "Vehicle return delayed beyond promised SLA window",
      2,
      "customer-0000",
      "cs_admin",
      "seed-ticket-" + tenantId + "-0001"
    ));
    saveTicket(new TicketRecord(
      "TKT-" + upperTenant + "-0002",
      tenantId,
      "ORD-" + upperTenant + "-0002",
      "CS_PORTAL",
      "RECONCILIATION",
      "P2",
      "IN_PROGRESS",
      "Invoice mismatch",
      "Customer reported mismatch between invoice and order total",
      1,
      "cs_admin",
      "fin_admin",
      "seed-ticket-" + tenantId + "-0002"
    ));
  }

  private void saveSettlement(String tenantId, Map<String, Object> settlement) {
    String settlementId = asString(settlement.get("settlementId"));
    settlementsByTenant
      .computeIfAbsent(tenantId, ignore -> new ConcurrentHashMap<>())
      .put(settlementId, Map.copyOf(settlement));
  }

  private void saveSettlementLines(String tenantId, String settlementId, List<Map<String, Object>> lines) {
    List<Map<String, Object>> immutableLines = lines.stream().map(Map::copyOf).toList();
    settlementLinesByTenant
      .computeIfAbsent(tenantId, ignore -> new ConcurrentHashMap<>())
      .put(settlementId, immutableLines);
  }

  private Map<String, Object> createSettlementRecord(
    String settlementId,
    String tenantId,
    String periodStart,
    String periodEnd,
    String status,
    double grossAmount,
    double netAmount,
    String currency,
    OffsetDateTime createdAt
  ) {
    Map<String, Object> settlement = new LinkedHashMap<>();
    settlement.put("settlementId", settlementId);
    settlement.put("tenantId", tenantId);
    settlement.put("periodStart", periodStart);
    settlement.put("periodEnd", periodEnd);
    settlement.put("status", status);
    settlement.put("grossAmount", grossAmount);
    settlement.put("netAmount", netAmount);
    settlement.put("currency", currency);
    settlement.put("createdAt", createdAt.toString());
    settlement.put("updatedAt", createdAt.toString());
    return settlement;
  }

  private Map<String, Object> createSettlementLineRecord(
    String settlementId,
    String lineNo,
    String beneficiaryType,
    String beneficiaryId,
    double amount,
    String ruleCode
  ) {
    Map<String, Object> line = new LinkedHashMap<>();
    line.put("lineId", settlementId + "-" + lineNo);
    line.put("settlementId", settlementId);
    line.put("beneficiaryType", beneficiaryType);
    line.put("beneficiaryId", beneficiaryId);
    line.put("amount", amount);
    line.put("currency", "CNY");
    line.put("ruleCode", ruleCode);
    return line;
  }

  private void saveInvestor(DeviceInvestor investor, List<String> cabinetIds) {
    investorsByTenant.computeIfAbsent(investor.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(investor.id(), investor);
    investorCabinetIdsByTenant.computeIfAbsent(investor.tenantId(), ignore -> new ConcurrentHashMap<>())
      .put(investor.id(), List.copyOf(cabinetIds));
  }

  private void seedStaffRoster(String tenantId, OffsetDateTime now) {
    saveStaff(tenantId, Map.of(
      "staffId", "STAFF-" + tenantId.toUpperCase() + "-TECH01",
      "tenantId", tenantId,
      "staffType", "TECHNICIAN",
      "employeeCode", "TECH-01",
      "displayName", "技师01",
      "status", "ACTIVE",
      "mobileMasked", "136****0009",
      "createdAt", now.toString(),
      "updatedAt", now.toString()
    ));
    saveStaff(tenantId, Map.of(
      "staffId", "STAFF-" + tenantId.toUpperCase() + "-VALET01",
      "tenantId", tenantId,
      "staffType", "VALET",
      "employeeCode", "VALET-01",
      "displayName", "代泊01",
      "status", "ACTIVE",
      "mobileMasked", "136****0010",
      "createdAt", now.toString(),
      "updatedAt", now.toString()
    ));
  }

  private boolean matchesStaffType(Map<String, Object> item, String staffType) {
    if (staffType == null || staffType.isBlank()) {
      return true;
    }
    return staffType.equalsIgnoreCase(asString(item.get("staffType")));
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private <T> List<T> slice(List<T> source, int page, int size) {
    int safePage = Math.max(page, 1);
    int safeSize = Math.max(size, 1);
    int from = (safePage - 1) * safeSize;
    if (from >= source.size()) {
      return List.of();
    }
    int to = Math.min(source.size(), from + safeSize);
    return source.subList(from, to);
  }
}
