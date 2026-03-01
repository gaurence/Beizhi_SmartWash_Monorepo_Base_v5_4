package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.Site;
import com.beizhi.smartwash.domain.model.SiteQueueItem;
import com.beizhi.smartwash.domain.model.Slot;
import com.beizhi.smartwash.domain.model.SlotState;
import com.beizhi.smartwash.domain.service.SlotStateMachine;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

@Service
public class AppCustomerService {
  private static final String MOCK_SMS_CODE = "123456";

  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final SlotStateMachine slotStateMachine;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public AppCustomerService(
    InMemoryPlatformRepository repository,
    PermissionGuard permissionGuard,
    SlotStateMachine slotStateMachine,
    IdempotencyService idempotencyService,
    AuditService auditService
  ) {
    this.repository = repository;
    this.permissionGuard = permissionGuard;
    this.slotStateMachine = slotStateMachine;
    this.idempotencyService = idempotencyService;
    this.auditService = auditService;
  }

  public Map<String, Object> sendSms(Map<String, Object> body) {
    String mobile = normalizeMobile(body.get("mobile"), body.get("phone"));
    return Map.of(
      "sent", true,
      "mobileMasked", maskMobile(mobile),
      "codeTtlSec", 300,
      "requestId", RequestContextHolder.get().requestId(),
      "sentAt", OffsetDateTime.now().toString()
    );
  }

  public Map<String, Object> smsLogin(Map<String, Object> body) {
    String mobile = normalizeMobile(body.get("mobile"), body.get("phone"));
    String code = asString(body.get("code"));
    if (!StringUtils.hasText(code)) {
      code = asString(body.get("otp"));
    }
    if (!MOCK_SMS_CODE.equals(code)) {
      throw new BadRequestException("Invalid SMS code");
    }

    String tenantId = tenantForMobile(mobile);
    String userId = "customer-" + mobile.substring(mobile.length() - 4);
    String token = "mock-jwt:" + tenantId + ":" + userId;

    return Map.of(
      "token", token,
      "tokenType", "Bearer",
      "expiresIn", 3600,
      "tenantId", tenantId,
      "userId", userId,
      "role", "CUSTOMER",
      "mobileMasked", maskMobile(mobile),
      "issuedAt", OffsetDateTime.now().toString()
    );
  }

  public Map<String, Object> logout() {
    RequestContext context = RequestContextHolder.get();
    return Map.of(
      "loggedOut", true,
      "tenantId", context.tenantId(),
      "requestId", context.requestId(),
      "loggedOutAt", OffsetDateTime.now().toString()
    );
  }

  public List<Site> listNearbySites() {
    permissionGuard.check("app.site.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.listSites(tenantId);
  }

  public Site getSite(String siteId) {
    permissionGuard.check("app.site.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.findSite(tenantId, siteId)
      .orElseThrow(() -> new NotFoundException("Site not found"));
  }

  public List<SiteQueueItem> getSiteQueue(String siteId) {
    permissionGuard.check("app.site.read");
    String tenantId = RequestContextHolder.get().tenantId();
    repository.findSite(tenantId, siteId)
      .orElseThrow(() -> new NotFoundException("Site not found"));
    return repository.listSiteQueue(tenantId, siteId);
  }

  public List<Map<String, Object>> scanCabinets(Map<String, Object> body) {
    permissionGuard.check("app.cabinet.scan");
    RequestContext context = RequestContextHolder.get();
    String keyword = asString(body.get("keyword")).toLowerCase(Locale.ROOT);
    String siteId = asString(body.get("siteId"));

    List<Map<String, Object>> scanned = new ArrayList<>();
    for (com.beizhi.smartwash.domain.model.Cabinet cabinet : repository.listCabinets(context.tenantId(), 1, 100)) {
      if (StringUtils.hasText(siteId) && !siteId.equals(cabinet.siteId())) {
        continue;
      }

      if (StringUtils.hasText(keyword)
        && !asString(cabinet.id()).toLowerCase(Locale.ROOT).contains(keyword)
        && !asString(cabinet.serialNo()).toLowerCase(Locale.ROOT).contains(keyword)) {
        continue;
      }

      List<Slot> slots = repository.listSlots(context.tenantId(), cabinet.id());
      long freeSlots = slots.stream().filter(slot -> slot.state() == SlotState.RESERVED).count();

      Map<String, Object> row = new LinkedHashMap<>();
      row.put("cabinetId", cabinet.id());
      row.put("siteId", cabinet.siteId());
      row.put("serialNo", cabinet.serialNo());
      row.put("status", cabinet.status());
      row.put("slotTotal", slots.size());
      row.put("slotAvailable", freeSlots);
      row.put("requestId", context.requestId());
      scanned.add(Map.copyOf(row));
    }

    return List.copyOf(scanned);
  }

  public Map<String, Object> depositKey(String orderId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("app.order.deposit-key");
    RequestContext context = RequestContextHolder.get();
    String cabinetId = asString(body.get("cabinetId"));
    String slotNo = asString(body.get("slotNo"));
    if (!StringUtils.hasText(cabinetId) || !StringUtils.hasText(slotNo)) {
      throw new BadRequestException("cabinetId and slotNo are required");
    }

    repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));
    ensureSlotExists(context.tenantId(), cabinetId, slotNo);

    String requestHash = context.tenantId() + "|" + orderId + "|" + cabinetId + "|" + slotNo + "|DEPOSIT_KEY";
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "app-order-deposit-key:" + orderId,
      idempotencyKey,
      requestHash,
      () -> repository.withSlotLock(
        context.tenantId(),
        cabinetId,
        slotNo,
        () -> doDepositKey(context, orderId, cabinetId, slotNo)
      )
    );

    return result;
  }

  public Map<String, Object> retrieveKey(String orderId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("app.order.retrieve-key");
    RequestContext context = RequestContextHolder.get();

    Map<String, Object> existingOrder = repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));

    String resolvedCabinetId = asString(body.get("cabinetId"));
    if (!StringUtils.hasText(resolvedCabinetId)) {
      resolvedCabinetId = asString(existingOrder.get("keyCabinetId"));
    }
    String resolvedSlotNo = asString(body.get("slotNo"));
    if (!StringUtils.hasText(resolvedSlotNo)) {
      resolvedSlotNo = asString(existingOrder.get("keySlotNo"));
    }
    if (!StringUtils.hasText(resolvedCabinetId) || !StringUtils.hasText(resolvedSlotNo)) {
      throw new BadRequestException("cabinetId and slotNo are required");
    }
    final String cabinetId = resolvedCabinetId;
    final String slotNo = resolvedSlotNo;

    ensureSlotExists(context.tenantId(), cabinetId, slotNo);
    String requestHash = context.tenantId() + "|" + orderId + "|" + cabinetId + "|" + slotNo + "|RETRIEVE_KEY";
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "app-order-retrieve-key:" + orderId,
      idempotencyKey,
      requestHash,
      () -> repository.withSlotLock(
        context.tenantId(),
        cabinetId,
        slotNo,
        () -> doRetrieveKey(context, orderId, cabinetId, slotNo)
      )
    );

    return result;
  }

  public List<Map<String, Object>> listOrders(int page, int size) {
    permissionGuard.check("app.order.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.listAppOrders(tenantId, page, size).stream()
      .map(this::toOrderView)
      .toList();
  }

  public long countOrders() {
    permissionGuard.check("app.order.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.countAppOrders(tenantId);
  }

  public Map<String, Object> createOrder(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("app.order.create");
    RequestContext context = RequestContextHolder.get();
    String requestHash = context.tenantId() + "|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> created = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "app-order-create",
      idempotencyKey,
      requestHash,
      () -> doCreateOrder(context, body)
    );
    return toOrderView(created);
  }

  public Map<String, Object> getOrder(String orderId) {
    permissionGuard.check("app.order.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.findAppOrder(tenantId, orderId)
      .map(this::toOrderView)
      .orElseThrow(() -> new NotFoundException("Order not found"));
  }

  public Map<String, Object> cancelOrder(String orderId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("app.order.create");
    RequestContext context = RequestContextHolder.get();
    String requestHash = context.tenantId() + "|" + orderId + "|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> cancelled = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "app-order-cancel:" + orderId,
      idempotencyKey,
      requestHash,
      () -> doCancelOrder(context, orderId, body)
    );
    return toOrderView(cancelled);
  }

  public Map<String, Object> issueDelegateRetrieveCode(String orderId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("app.order.delegate");
    RequestContext context = RequestContextHolder.get();
    String requestHash = context.tenantId() + "|" + orderId + "|" + String.valueOf(body);

    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) idempotencyService.execute(
      context.tenantId(),
      "app-order-delegate-retrieve-code:" + orderId,
      idempotencyKey,
      requestHash,
      () -> doIssueDelegateRetrieveCode(context, orderId, body)
    );
    return result;
  }

  public List<Map<String, Object>> getOrderTimeline(String orderId) {
    permissionGuard.check("app.order.read");
    String tenantId = RequestContextHolder.get().tenantId();
    repository.findAppOrder(tenantId, orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));
    return repository.listAppOrderTimeline(tenantId, orderId);
  }

  private Map<String, Object> doDepositKey(RequestContext context, String orderId, String cabinetId, String slotNo) {
    Map<String, Object> order = repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));
    String status = asString(order.get("status")).toUpperCase(Locale.ROOT);
    if ("KEY_DEPOSITED".equals(status)
      && cabinetId.equals(asString(order.get("keyCabinetId")))
      && slotNo.equals(asString(order.get("keySlotNo")))) {
      return keyTransitionResult(orderId, "KEY_DEPOSITED", cabinetId, slotNo, asString(order.get("keyDepositedAt")));
    }
    if ("CANCELLED".equals(status) || "COMPLETED".equals(status) || "CLOSED".equals(status)) {
      throw new BadRequestException("Order cannot deposit key in status: " + status);
    }

    Slot slot = repository.findSlot(context.tenantId(), cabinetId, slotNo)
      .orElseThrow(() -> new NotFoundException("Slot not found"));
    SlotState fromState = slot.state();
    if (fromState != SlotState.RESERVED) {
      throw new ConflictException("Slot is not available for deposit in state: " + fromState);
    }

    slotStateMachine.assertTransitionAllowed(fromState, SlotState.OCCUPIED);
    slot.transitionTo(SlotState.OCCUPIED);
    repository.saveSlot(slot);

    OffsetDateTime now = OffsetDateTime.now();
    Map<String, Object> updated = new LinkedHashMap<>(order);
    updated.put("status", "KEY_DEPOSITED");
    updated.put("keyCabinetId", cabinetId);
    updated.put("keySlotNo", slotNo);
    updated.put("keyDepositedAt", now.toString());
    updated.put("updatedAt", now.toString());
    updated.put("requestId", context.requestId());
    repository.saveAppOrder(context.tenantId(), updated);

    repository.appendAppOrderTimeline(
      context.tenantId(),
      orderId,
      "ORDER_KEY_DEPOSITED",
      "Key Deposited",
      "Customer key has been deposited into cabinet slot"
    );
    repository.appendEvent(
      context.tenantId(),
      cabinetId,
      "ORDER_KEY_DEPOSITED",
      Map.of("orderId", orderId, "slotNo", slotNo, "requestId", context.requestId())
    );
    auditService.append(
      "APP_ORDER_KEY_DEPOSIT",
      "app-order:" + orderId,
      "SUCCESS",
      Map.of("orderId", orderId, "cabinetId", cabinetId, "slotNo", slotNo, "fromState", fromState.name())
    );

    return keyTransitionResult(orderId, "KEY_DEPOSITED", cabinetId, slotNo, now.toString());
  }

  private Map<String, Object> doRetrieveKey(RequestContext context, String orderId, String cabinetId, String slotNo) {
    Map<String, Object> order = repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));
    String status = asString(order.get("status")).toUpperCase(Locale.ROOT);
    if ("KEY_RETRIEVED".equals(status)
      && cabinetId.equals(asString(order.get("keyCabinetId")))
      && slotNo.equals(asString(order.get("keySlotNo")))) {
      return keyTransitionResult(orderId, "KEY_RETRIEVED", cabinetId, slotNo, asString(order.get("keyRetrievedAt")));
    }
    if (!"KEY_DEPOSITED".equals(status)) {
      throw new BadRequestException("Order cannot retrieve key in status: " + status);
    }

    Slot slot = repository.findSlot(context.tenantId(), cabinetId, slotNo)
      .orElseThrow(() -> new NotFoundException("Slot not found"));
    SlotState fromState = slot.state();
    if (fromState != SlotState.OCCUPIED) {
      throw new ConflictException("Slot is not retrievable in state: " + fromState);
    }

    slotStateMachine.assertTransitionAllowed(fromState, SlotState.RESERVED);
    slot.transitionTo(SlotState.RESERVED);
    repository.saveSlot(slot);

    OffsetDateTime now = OffsetDateTime.now();
    Map<String, Object> updated = new LinkedHashMap<>(order);
    updated.put("status", "KEY_RETRIEVED");
    updated.put("keyCabinetId", cabinetId);
    updated.put("keySlotNo", slotNo);
    updated.put("keyRetrievedAt", now.toString());
    updated.put("updatedAt", now.toString());
    updated.put("requestId", context.requestId());
    repository.saveAppOrder(context.tenantId(), updated);

    repository.appendAppOrderTimeline(
      context.tenantId(),
      orderId,
      "ORDER_KEY_RETRIEVED",
      "Key Retrieved",
      "Customer key has been retrieved from cabinet slot"
    );
    repository.appendEvent(
      context.tenantId(),
      cabinetId,
      "ORDER_KEY_RETRIEVED",
      Map.of("orderId", orderId, "slotNo", slotNo, "requestId", context.requestId())
    );
    auditService.append(
      "APP_ORDER_KEY_RETRIEVE",
      "app-order:" + orderId,
      "SUCCESS",
      Map.of("orderId", orderId, "cabinetId", cabinetId, "slotNo", slotNo, "fromState", fromState.name())
    );

    return keyTransitionResult(orderId, "KEY_RETRIEVED", cabinetId, slotNo, now.toString());
  }

  private void ensureSlotExists(String tenantId, String cabinetId, String slotNo) {
    if (repository.findSlot(tenantId, cabinetId, slotNo).isEmpty()) {
      throw new NotFoundException("Slot not found");
    }
  }

  private Map<String, Object> keyTransitionResult(
    String orderId,
    String status,
    String cabinetId,
    String slotNo,
    String happenedAt
  ) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("orderId", orderId);
    result.put("status", status);
    result.put("cabinetId", cabinetId);
    result.put("slotNo", slotNo);
    result.put("happenedAt", happenedAt);
    result.put("requestId", RequestContextHolder.get().requestId());
    return result;
  }

  private Map<String, Object> doIssueDelegateRetrieveCode(RequestContext context, String orderId, Map<String, Object> body) {
    Map<String, Object> order = repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));

    String status = asString(order.get("status")).toUpperCase(Locale.ROOT);
    if ("CANCELLED".equals(status) || "COMPLETED".equals(status) || "CLOSED".equals(status)) {
      throw new BadRequestException("Order cannot issue delegate code in status: " + status);
    }

    String cabinetId = asString(body.get("cabinetId"));
    String slotId = asString(body.get("slotId"));
    if (!StringUtils.hasText(cabinetId) || !StringUtils.hasText(slotId)) {
      throw new BadRequestException("cabinetId and slotId are required");
    }

    int ttlSeconds = parseInt(body.get("ttlSeconds"), 300);
    if (ttlSeconds < 30 || ttlSeconds > 600) {
      throw new BadRequestException("ttlSeconds must be between 30 and 600");
    }

    String delegateMobile = asString(body.get("delegateMobile"));
    String delegateMobileMasked = "";
    if (StringUtils.hasText(delegateMobile)) {
      if (!delegateMobile.matches("^\\d{11}$")) {
        throw new BadRequestException("delegateMobile must be 11 digits");
      }
      delegateMobileMasked = maskMobile(delegateMobile);
    }

    OffsetDateTime now = OffsetDateTime.now();
    String delegateCode = generateDelegateCode();
    String delegateToken = "delegate:" + context.tenantId() + ":" + orderId + ":" + UUID.randomUUID();
    String expiresAt = now.plusSeconds(ttlSeconds).toString();

    Map<String, Object> updated = new LinkedHashMap<>(order);
    updated.put("delegateRetrieveCode", delegateCode);
    updated.put("delegateRetrieveToken", delegateToken);
    updated.put("delegateAction", "RETRIEVE_KEY");
    updated.put("delegateCabinetId", cabinetId);
    updated.put("delegateSlotId", slotId);
    updated.put("delegateIssuedBy", context.userId());
    updated.put("delegateIssuedAt", now.toString());
    updated.put("delegateExpiresAt", expiresAt);
    updated.put("delegateMobileMasked", delegateMobileMasked);
    updated.put("updatedAt", now.toString());
    updated.put("requestId", context.requestId());
    repository.saveAppOrder(context.tenantId(), updated);

    repository.appendAppOrderTimeline(
      context.tenantId(),
      orderId,
      "ORDER_DELEGATE_RETRIEVE_CODE_ISSUED",
      "Delegate Retrieve Code Issued",
      "Delegate retrieve code is valid for a short TTL"
    );

    auditService.append(
      "APP_ORDER_DELEGATE_RETRIEVE_CODE_ISSUE",
      "app-order:" + orderId,
      "SUCCESS",
      Map.of(
        "orderId", orderId,
        "action", "RETRIEVE_KEY",
        "cabinetId", cabinetId,
        "slotId", slotId,
        "ttlSeconds", ttlSeconds,
        "delegateMobileMasked", delegateMobileMasked
      )
    );

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("orderId", orderId);
    response.put("delegateRetrieveCode", delegateCode);
    response.put("delegateRetrieveToken", delegateToken);
    response.put("action", "RETRIEVE_KEY");
    response.put("cabinetId", cabinetId);
    response.put("slotId", slotId);
    response.put("ttlSeconds", ttlSeconds);
    response.put("issuedAt", now.toString());
    response.put("expiresAt", expiresAt);
    response.put("delegateMobileMasked", delegateMobileMasked);
    response.put("requestId", context.requestId());
    return response;
  }

  private Map<String, Object> doCancelOrder(RequestContext context, String orderId, Map<String, Object> body) {
    Map<String, Object> order = repository.findAppOrder(context.tenantId(), orderId)
      .orElseThrow(() -> new NotFoundException("Order not found"));

    String currentStatus = asString(order.get("status")).toUpperCase(Locale.ROOT);
    if ("CANCELLED".equals(currentStatus)) {
      return order;
    }
    if ("COMPLETED".equals(currentStatus) || "CLOSED".equals(currentStatus)) {
      throw new BadRequestException("Order cannot be cancelled in status: " + currentStatus);
    }

    OffsetDateTime now = OffsetDateTime.now();
    String reason = asString(body.get("reason"));
    if (!StringUtils.hasText(reason)) {
      reason = "CUSTOMER_REQUESTED";
    }

    Map<String, Object> updated = new LinkedHashMap<>(order);
    updated.put("status", "CANCELLED");
    updated.put("cancelReason", reason);
    updated.put("cancelledAt", now.toString());
    updated.put("updatedAt", now.toString());
    updated.put("requestId", context.requestId());

    repository.saveAppOrder(context.tenantId(), updated);
    repository.appendAppOrderTimeline(
      context.tenantId(),
      orderId,
      "ORDER_CANCELLED",
      "Order Cancelled",
      "Order cancelled by customer"
    );
    auditService.append(
      "APP_ORDER_CANCEL",
      "app-order:" + orderId,
      "SUCCESS",
      Map.of(
        "orderId", orderId,
        "fromStatus", currentStatus,
        "reason", reason
      )
    );
    return updated;
  }

  private Map<String, Object> doCreateOrder(RequestContext context, Map<String, Object> body) {
    OffsetDateTime now = OffsetDateTime.now();
    String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    String servicePackageId = asString(body.get("servicePackageId"));
    if (!StringUtils.hasText(servicePackageId)) {
      servicePackageId = "SP-BASIC";
    }
    String servicePackageName = asString(body.get("servicePackageName"));
    if (!StringUtils.hasText(servicePackageName)) {
      servicePackageName = "Standard Care";
    }
    String fulfillmentMode = asString(body.get("fulfillmentMode"));
    if (!StringUtils.hasText(fulfillmentMode)) {
      fulfillmentMode = "ONSITE";
    } else {
      fulfillmentMode = fulfillmentMode.toUpperCase(Locale.ROOT);
    }

    double amountSubtotal = toAmount(body.get("amountSubtotal"), 99d);
    double amountDiscount = Math.max(toAmount(body.get("amountDiscount"), 0d), 0d);
    double amountTotal = toAmount(body.get("amountTotal"), Math.max(amountSubtotal - amountDiscount, 0d));
    List<String> addonIds = normalizeStringList(body.get("addonIds"));
    String couponCode = asString(body.get("couponCode"));

    Map<String, Object> order = new LinkedHashMap<>();
    order.put("orderId", orderId);
    order.put("tenantId", context.tenantId());
    order.put("customerId", context.userId());
    order.put("status", "CREATED");
    order.put("fulfillmentMode", fulfillmentMode);
    order.put("servicePackageId", servicePackageId);
    order.put("servicePackageName", servicePackageName);
    order.put("addonIds", addonIds);
    order.put("couponCode", couponCode);
    order.put("currency", "CNY");
    order.put("amountSubtotal", amountSubtotal);
    order.put("amountDiscount", amountDiscount);
    order.put("amountTotal", amountTotal);
    order.put("createdAt", now.toString());
    order.put("updatedAt", now.toString());
    order.put("requestId", context.requestId());

    repository.saveAppOrder(context.tenantId(), order);
    repository.appendAppOrderTimeline(
      context.tenantId(),
      orderId,
      "ORDER_CREATED",
      "Order Created",
      "Price snapshot fixed; awaiting fulfillment"
    );

    auditService.append(
      "APP_ORDER_CREATE",
      "app-order:" + orderId,
      "SUCCESS",
      Map.of(
        "orderId", orderId,
        "fulfillmentMode", fulfillmentMode,
        "servicePackageId", servicePackageId
      )
    );

    return order;
  }

  private Map<String, Object> toOrderView(Map<String, Object> order) {
    Map<String, Object> view = new LinkedHashMap<>();
    view.put("orderId", asString(order.get("orderId")));
    view.put("customerId", asString(order.get("customerId")));
    view.put("status", asString(order.get("status")));
    view.put("fulfillmentMode", asString(order.get("fulfillmentMode")));
    view.put("servicePackageId", asString(order.get("servicePackageId")));
    view.put("servicePackageName", asString(order.get("servicePackageName")));
    view.put("addonIds", normalizeStringList(order.get("addonIds")));
    view.put("couponCode", asString(order.get("couponCode")));
    view.put("currency", asString(order.get("currency")));
    view.put("amountSubtotal", toAmount(order.get("amountSubtotal"), 0d));
    view.put("amountDiscount", toAmount(order.get("amountDiscount"), 0d));
    view.put("amountTotal", toAmount(order.get("amountTotal"), 0d));
    view.put("createdAt", asString(order.get("createdAt")));
    view.put("updatedAt", asString(order.get("updatedAt")));
    view.put("requestId", asString(order.get("requestId")));
    return view;
  }

  private List<String> normalizeStringList(Object value) {
    if (!(value instanceof List<?> raw)) {
      return List.of();
    }
    List<String> result = new ArrayList<>();
    for (Object item : raw) {
      String text = asString(item);
      if (StringUtils.hasText(text)) {
        result.add(text);
      }
    }
    return List.copyOf(result);
  }

  private double toAmount(Object value, double defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    if (value instanceof Number n) {
      return n.doubleValue();
    }
    String text = asString(value);
    if (!StringUtils.hasText(text)) {
      return defaultValue;
    }
    try {
      return Double.parseDouble(text);
    } catch (NumberFormatException ex) {
      throw new BadRequestException("Invalid amount: " + text);
    }
  }

  private String normalizeMobile(Object first, Object second) {
    String mobile = asString(first);
    if (!StringUtils.hasText(mobile)) {
      mobile = asString(second);
    }
    if (!mobile.matches("^\\d{11}$")) {
      throw new BadRequestException("mobile must be 11 digits");
    }
    return mobile;
  }

  private String tenantForMobile(String mobile) {
    if ("13800000000".equals(mobile)) {
      return "tenant-a";
    }
    if ("13800000001".equals(mobile)) {
      return "tenant-b";
    }
    return "tenant-a";
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value).trim();
  }

  private int parseInt(Object value, int defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    String text = asString(value);
    if (!StringUtils.hasText(text)) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(text);
    } catch (NumberFormatException ex) {
      throw new BadRequestException("invalid integer value: " + text);
    }
  }

  private String generateDelegateCode() {
    int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
    return String.valueOf(code);
  }

  private String maskMobile(String mobile) {
    return mobile.substring(0, 3) + "****" + mobile.substring(7);
  }
}
