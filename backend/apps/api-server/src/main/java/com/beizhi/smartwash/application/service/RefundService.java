package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.RefundRequestRecord;
import com.beizhi.smartwash.infrastructure.repository.InMemoryPlatformRepository;
import com.beizhi.smartwash.interfaces.rest.error.BadRequestException;
import com.beizhi.smartwash.interfaces.rest.error.NotFoundException;
import com.beizhi.smartwash.security.PermissionGuard;
import com.beizhi.smartwash.security.RequestContext;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RefundService {
  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public RefundService(
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

  public List<RefundRequestRecord> listRefunds(int page, int size) {
    permissionGuard.check("finance.refund.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.listRefunds(tenantId, page, size);
  }

  public long countRefunds() {
    return repository.countRefunds(RequestContextHolder.get().tenantId());
  }

  public Object createRefund(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("finance.refund.manage");
    RequestContext context = RequestContextHolder.get();

    String orderId = requireText(body.get("orderId"), "orderId is required");
    String reason = requireText(body.get("reason"), "reason is required");
    double amount = requirePositiveAmount(body.get("amount"));
    String currency = normalizeCurrency(body.get("currency"));
    String requestHash = context.tenantId() + "|" + orderId + "|" + amount + "|" + currency + "|" + reason;

    return idempotencyService.execute(context.tenantId(), "finance-refund-create", idempotencyKey, requestHash, () -> {
      RefundRequestRecord refund = new RefundRequestRecord(
        UUID.randomUUID().toString(),
        context.tenantId(),
        orderId,
        amount,
        currency,
        reason,
        "PENDING_APPROVAL",
        context.userId(),
        context.requestId()
      );
      repository.saveRefund(refund);
      auditService.append(
        "REFUND_REQUEST_CREATE",
        "refund:" + refund.refundId(),
        "SUCCESS",
        Map.of(
          "refundId", refund.refundId(),
          "orderId", orderId,
          "amount", amount,
          "currency", currency,
          "status", refund.status()
        )
      );
      return refund;
    });
  }

  public Object approveRefund(String refundId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("finance.refund.manage");
    RequestContext context = RequestContextHolder.get();
    String requestHash = context.tenantId() + "|" + refundId + "|" + String.valueOf(body);

    return idempotencyService.execute(
      context.tenantId(),
      "finance-refund-approve:" + refundId,
      idempotencyKey,
      requestHash,
      () -> doApprove(context, refundId)
    );
  }

  private RefundRequestRecord doApprove(RequestContext context, String refundId) {
    RefundRequestRecord refund = repository.findRefund(context.tenantId(), refundId)
      .orElseThrow(() -> new NotFoundException("Refund not found"));

    String fromStatus = refund.status();
    if ("APPROVED".equalsIgnoreCase(fromStatus)) {
      return refund;
    }
    if (!"PENDING_APPROVAL".equalsIgnoreCase(fromStatus)) {
      throw new BadRequestException("Refund cannot be approved in status: " + fromStatus);
    }

    refund.approve(context.userId(), context.requestId());
    auditService.append(
      "REFUND_APPROVE",
      "refund:" + refundId,
      "SUCCESS",
      Map.of(
        "refundId", refundId,
        "orderId", refund.orderId(),
        "fromStatus", fromStatus,
        "toStatus", refund.status()
      )
    );
    return refund;
  }

  private String requireText(Object value, String message) {
    String text = asString(value).trim();
    if (!StringUtils.hasText(text)) {
      throw new BadRequestException(message);
    }
    return text;
  }

  private double requirePositiveAmount(Object value) {
    String raw = asString(value).trim();
    if (!StringUtils.hasText(raw)) {
      throw new BadRequestException("amount is required");
    }
    try {
      double amount = Double.parseDouble(raw);
      if (amount <= 0) {
        throw new BadRequestException("amount must be greater than 0");
      }
      return amount;
    } catch (NumberFormatException ex) {
      throw new BadRequestException("amount must be a number");
    }
  }

  private String normalizeCurrency(Object value) {
    String currency = asString(value).trim().toUpperCase();
    if (!StringUtils.hasText(currency)) {
      return "CNY";
    }
    return currency;
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
