package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.AppCustomerService;
import com.beizhi.smartwash.common.ApiResponse;
import com.beizhi.smartwash.common.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AppCustomerController {
  private final AppCustomerService appCustomerService;

  public AppCustomerController(AppCustomerService appCustomerService) {
    this.appCustomerService = appCustomerService;
  }

  @PostMapping("/app/auth/sms/send")
  public ApiResponse sendSms(@RequestBody(required = false) Map<String, Object> body) {
    return ApiResponse.ok(appCustomerService.sendSms(body == null ? Map.of() : body));
  }

  @PostMapping("/app/auth/sms/login")
  public ApiResponse smsLogin(@RequestBody(required = false) Map<String, Object> body) {
    return ApiResponse.ok(appCustomerService.smsLogin(body == null ? Map.of() : body));
  }

  @PostMapping("/app/auth/logout")
  public ApiResponse logout() {
    return ApiResponse.ok(appCustomerService.logout());
  }

  @PostMapping("/app/cabinets/scan")
  public ApiResponse scanCabinets(
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(appCustomerService.scanCabinets(body == null ? Map.of() : body));
  }

  @GetMapping("/app/sites/nearby")
  public ApiResponse nearbySites() {
    return ApiResponse.ok(appCustomerService.listNearbySites());
  }

  @GetMapping("/app/sites/{siteId}")
  public ApiResponse getSite(@PathVariable("siteId") String siteId) {
    return ApiResponse.ok(appCustomerService.getSite(siteId));
  }

  @GetMapping("/app/sites/{siteId}/queue")
  public ApiResponse getSiteQueue(@PathVariable("siteId") String siteId) {
    return ApiResponse.ok(appCustomerService.getSiteQueue(siteId));
  }

  @GetMapping("/app/orders")
  public PageResponse listOrders(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(
      appCustomerService.listOrders(page, size),
      page,
      size,
      appCustomerService.countOrders()
    );
  }

  @PostMapping("/app/orders")
  public ApiResponse createOrder(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(appCustomerService.createOrder(body == null ? Map.of() : body, idempotencyKey));
  }

  @GetMapping("/app/orders/{orderId}")
  public ApiResponse getOrder(@PathVariable("orderId") String orderId) {
    return ApiResponse.ok(appCustomerService.getOrder(orderId));
  }

  @PostMapping("/app/orders/{orderId}/cancel")
  public ApiResponse cancelOrder(
    @PathVariable("orderId") String orderId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(appCustomerService.cancelOrder(orderId, body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/app/orders/{orderId}/deposit-key")
  public ApiResponse depositKey(
    @PathVariable("orderId") String orderId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(appCustomerService.depositKey(orderId, body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/app/orders/{orderId}/retrieve-key")
  public ApiResponse retrieveKey(
    @PathVariable("orderId") String orderId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(appCustomerService.retrieveKey(orderId, body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/app/orders/{orderId}/delegate-retrieve-code")
  public ApiResponse issueDelegateRetrieveCode(
    @PathVariable("orderId") String orderId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(
      appCustomerService.issueDelegateRetrieveCode(orderId, body == null ? Map.of() : body, idempotencyKey)
    );
  }

  @GetMapping("/app/orders/{orderId}/timeline")
  public ApiResponse getOrderTimeline(@PathVariable("orderId") String orderId) {
    return ApiResponse.ok(appCustomerService.getOrderTimeline(orderId));
  }
}
