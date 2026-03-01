package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.OrderService;
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
public class OrderController {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping("/orders")
  public PageResponse listOrders(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(
      orderService.listOrders(page, size),
      page,
      size,
      orderService.countOrders()
    );
  }

  @PostMapping("/orders")
  public ApiResponse createOrder(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(orderService.createOrder(body == null ? Map.of() : body, idempotencyKey));
  }

  @GetMapping("/orders/{orderId}")
  public ApiResponse getOrder(@PathVariable("orderId") String orderId) {
    return ApiResponse.ok(orderService.getOrder(orderId));
  }

  @PostMapping("/orders/{orderId}/cancel")
  public ApiResponse cancelOrder(
    @PathVariable("orderId") String orderId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(orderService.cancelOrder(orderId, body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/orders/{orderId}/dispatch")
  public ApiResponse dispatchOrder(
    @PathVariable("orderId") String orderId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(orderService.dispatchOrder(orderId, body == null ? Map.of() : body, idempotencyKey));
  }

  @GetMapping("/orders/{orderId}/evidence")
  public ApiResponse getOrderEvidence(@PathVariable("orderId") String orderId) {
    return ApiResponse.ok(orderService.getOrderEvidence(orderId));
  }

  @GetMapping("/orders/{orderId}/timeline")
  public ApiResponse getOrderTimeline(@PathVariable("orderId") String orderId) {
    return ApiResponse.ok(orderService.getOrderTimeline(orderId));
  }

  @PostMapping("/evidence/upload-url")
  public ApiResponse issueEvidenceUploadUrl(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(orderService.issueEvidenceUploadUrl(body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/evidence/records")
  public ApiResponse appendEvidenceRecord(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(orderService.appendEvidenceRecord(body == null ? Map.of() : body, idempotencyKey));
  }
}
