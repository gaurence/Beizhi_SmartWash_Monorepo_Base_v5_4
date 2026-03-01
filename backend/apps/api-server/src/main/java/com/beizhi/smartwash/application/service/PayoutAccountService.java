package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.PayoutAccount;
import com.beizhi.smartwash.infrastructure.repository.InMemoryPlatformRepository;
import com.beizhi.smartwash.interfaces.rest.error.BadRequestException;
import com.beizhi.smartwash.interfaces.rest.error.NotFoundException;
import com.beizhi.smartwash.security.PermissionGuard;
import com.beizhi.smartwash.security.RequestContext;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PayoutAccountService {
  private static final Set<String> SUPPORTED_ACCOUNT_TYPES = Set.of("BANK", "ALIPAY", "WECHAT", "OTHER");
  private static final Set<String> SUPPORTED_STATUSES = Set.of("ACTIVE", "INACTIVE", "VERIFYING");

  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public PayoutAccountService(
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

  public List<PayoutAccount> listAccounts(int page, int size) {
    permissionGuard.check("finance.payout.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.listPayoutAccounts(tenantId, page, size);
  }

  public long countAccounts() {
    return repository.countPayoutAccounts(RequestContextHolder.get().tenantId());
  }

  public Object createAccount(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("finance.payout.manage");
    RequestContext context = RequestContextHolder.get();

    String ownerOrgId = requireText(body.get("ownerOrgId"), "ownerOrgId is required");
    String accountType = normalizeAccountType(body.get("accountType"), true);
    String accountNo = requireText(body.get("accountNo"), "accountNo is required");
    String holderName = requireText(body.get("holderName"), "holderName is required");
    String channelMerchantId = asString(body.get("channelMerchantId")).trim();
    String accountNoMasked = maskAccountNo(accountNo);
    String accountNoEnc = encodeAccountNo(context.tenantId(), accountNo);
    String requestHash = context.tenantId() + "|" + ownerOrgId + "|" + accountType + "|" + accountNoMasked + "|" + holderName + "|" + channelMerchantId;

    return idempotencyService.execute(context.tenantId(), "finance-payout-create", idempotencyKey, requestHash, () -> {
      PayoutAccount account = new PayoutAccount(
        UUID.randomUUID().toString(),
        context.tenantId(),
        ownerOrgId,
        accountType,
        accountNoMasked,
        accountNoEnc,
        holderName,
        channelMerchantId,
        "VERIFYING"
      );
      repository.savePayoutAccount(account);
      auditService.append(
        "PAYOUT_ACCOUNT_CREATE",
        "payout-account:" + account.id(),
        "SUCCESS",
        Map.of(
          "accountId", account.id(),
          "ownerOrgId", ownerOrgId,
          "accountType", accountType,
          "status", account.status()
        )
      );
      return account;
    });
  }

  public PayoutAccount updateAccount(String accountId, Map<String, Object> body) {
    permissionGuard.check("finance.payout.manage");
    RequestContext context = RequestContextHolder.get();

    PayoutAccount account = repository.findPayoutAccount(context.tenantId(), accountId)
      .orElseThrow(() -> new NotFoundException("Payout account not found"));

    String ownerOrgId = asString(body.get("ownerOrgId")).trim();
    String accountType = normalizeAccountType(body.get("accountType"), false);
    String holderName = asString(body.get("holderName")).trim();
    String channelMerchantId = asString(body.get("channelMerchantId")).trim();
    String status = normalizeStatus(body.get("status"), false);

    String accountNo = asString(body.get("accountNo")).trim();
    String accountNoMasked = "";
    String accountNoEnc = "";
    if (StringUtils.hasText(accountNo)) {
      accountNoMasked = maskAccountNo(accountNo);
      accountNoEnc = encodeAccountNo(context.tenantId(), accountNo);
    }

    account.update(ownerOrgId, accountType, accountNoMasked, accountNoEnc, holderName, channelMerchantId, status);

    auditService.append(
      "PAYOUT_ACCOUNT_UPDATE",
      "payout-account:" + account.id(),
      "SUCCESS",
      Map.of(
        "accountId", account.id(),
        "ownerOrgId", account.ownerOrgId(),
        "accountType", account.accountType(),
        "status", account.status()
      )
    );
    return account;
  }

  public Object submitReview(String accountId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("finance.payout.manage");
    RequestContext context = RequestContextHolder.get();

    PayoutAccount account = repository.findPayoutAccount(context.tenantId(), accountId)
      .orElseThrow(() -> new NotFoundException("Payout account not found"));

    String reviewerNote = asString(body.get("reviewerNote")).trim();
    String requestHash = context.tenantId() + "|" + accountId + "|" + reviewerNote;

    return idempotencyService.execute(context.tenantId(), "finance-payout-submit-review", idempotencyKey, requestHash, () -> {
      account.submitReview(context.userId());
      auditService.append(
        "PAYOUT_ACCOUNT_SUBMIT_REVIEW",
        "payout-account:" + account.id(),
        "SUCCESS",
        Map.of(
          "accountId", account.id(),
          "status", account.status(),
          "submittedBy", context.userId(),
          "reviewerNoteProvided", StringUtils.hasText(reviewerNote)
        )
      );
      return Map.of(
        "accountId", account.id(),
        "status", account.status(),
        "submittedBy", context.userId(),
        "requestId", context.requestId()
      );
    });
  }

  private String requireText(Object value, String message) {
    String text = asString(value).trim();
    if (!StringUtils.hasText(text)) {
      throw new BadRequestException(message);
    }
    return text;
  }

  private String normalizeAccountType(Object value, boolean required) {
    String accountType = asString(value).trim().toUpperCase();
    if (!StringUtils.hasText(accountType)) {
      if (required) {
        throw new BadRequestException("accountType is required");
      }
      return "";
    }
    if (!SUPPORTED_ACCOUNT_TYPES.contains(accountType)) {
      throw new BadRequestException("Unsupported accountType: " + value);
    }
    return accountType;
  }

  private String normalizeStatus(Object value, boolean required) {
    String status = asString(value).trim().toUpperCase();
    if (!StringUtils.hasText(status)) {
      if (required) {
        throw new BadRequestException("status is required");
      }
      return "";
    }
    if (!SUPPORTED_STATUSES.contains(status)) {
      throw new BadRequestException("Unsupported status: " + value);
    }
    return status;
  }

  private String maskAccountNo(String accountNo) {
    String digits = accountNo.replaceAll("\\s+", "");
    if (digits.length() < 6) {
      throw new BadRequestException("accountNo must be at least 6 characters");
    }
    return digits.substring(0, 2) + "****" + digits.substring(digits.length() - 4);
  }

  private String encodeAccountNo(String tenantId, String accountNo) {
    String raw = tenantId + "|" + accountNo;
    return "enc:" + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
