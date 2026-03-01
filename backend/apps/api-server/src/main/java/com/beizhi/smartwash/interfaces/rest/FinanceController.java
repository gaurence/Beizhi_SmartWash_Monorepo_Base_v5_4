package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.PayoutAccountService;
import com.beizhi.smartwash.application.service.RefundService;
import com.beizhi.smartwash.application.service.SettlementService;
import com.beizhi.smartwash.application.service.TicketService;
import com.beizhi.smartwash.common.ApiResponse;
import com.beizhi.smartwash.common.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FinanceController {
  private final PayoutAccountService payoutAccountService;
  private final RefundService refundService;
  private final SettlementService settlementService;
  private final TicketService ticketService;

  public FinanceController(
    PayoutAccountService payoutAccountService,
    RefundService refundService,
    SettlementService settlementService,
    TicketService ticketService
  ) {
    this.payoutAccountService = payoutAccountService;
    this.refundService = refundService;
    this.settlementService = settlementService;
    this.ticketService = ticketService;
  }

  @GetMapping("/finance/payout-accounts")
  public PageResponse listPayoutAccounts(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(
      payoutAccountService.listAccounts(page, size),
      page,
      size,
      payoutAccountService.countAccounts()
    );
  }

  @PostMapping("/finance/payout-accounts")
  public ApiResponse createPayoutAccount(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(payoutAccountService.createAccount(body == null ? Map.of() : body, idempotencyKey));
  }

  @PatchMapping("/finance/payout-accounts/{accountId}")
  public ApiResponse updatePayoutAccount(
    @PathVariable("accountId") String accountId,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(payoutAccountService.updateAccount(accountId, body == null ? Map.of() : body));
  }

  @PostMapping("/finance/payout-accounts/{accountId}/submit-review")
  public ApiResponse submitPayoutAccountReview(
    @PathVariable("accountId") String accountId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(
      payoutAccountService.submitReview(accountId, body == null ? Map.of() : body, idempotencyKey)
    );
  }

  @GetMapping("/finance/refunds")
  public PageResponse listRefunds(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(
      refundService.listRefunds(page, size),
      page,
      size,
      refundService.countRefunds()
    );
  }

  @PostMapping("/finance/refunds")
  public ApiResponse createRefund(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(refundService.createRefund(body == null ? Map.of() : body, idempotencyKey));
  }

  @GetMapping("/finance/reconciliation/tickets")
  public PageResponse listReconciliationTickets(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(
      ticketService.listTickets(page, size),
      page,
      size,
      ticketService.countTickets()
    );
  }

  @PostMapping("/finance/reconciliation/tickets")
  public ApiResponse createReconciliationTicket(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(ticketService.createTicket(body == null ? Map.of() : body, idempotencyKey));
  }

  @GetMapping("/finance/reconciliation/tickets/{ticketId}")
  public ApiResponse getReconciliationTicket(@PathVariable("ticketId") String ticketId) {
    return ApiResponse.ok(ticketService.getTicket(ticketId));
  }

  @PostMapping("/finance/refunds/{refundId}/approve")
  public ApiResponse approveRefund(
    @PathVariable("refundId") String refundId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(
      refundService.approveRefund(refundId, body == null ? Map.of() : body, idempotencyKey)
    );
  }

  @GetMapping("/finance/settlements")
  public PageResponse listSettlements(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(
      settlementService.listSettlements(page, size),
      page,
      size,
      settlementService.countSettlements()
    );
  }

  @GetMapping("/finance/settlements/{settlementId}")
  public ApiResponse getSettlement(@PathVariable("settlementId") String settlementId) {
    return ApiResponse.ok(settlementService.getSettlement(settlementId));
  }

  @GetMapping("/finance/settlements/{settlementId}/lines")
  public PageResponse listSettlementLines(
    @PathVariable("settlementId") String settlementId,
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(
      settlementService.listSettlementLines(settlementId, page, size),
      page,
      size,
      settlementService.countSettlementLines(settlementId)
    );
  }

  @PostMapping("/finance/settlements/{settlementId}/export")
  public ApiResponse exportSettlement(
    @PathVariable("settlementId") String settlementId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(
      settlementService.exportSettlement(settlementId, body == null ? Map.of() : body, idempotencyKey)
    );
  }
}
