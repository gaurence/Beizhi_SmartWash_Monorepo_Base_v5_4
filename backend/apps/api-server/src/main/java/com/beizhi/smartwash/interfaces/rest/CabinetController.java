package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.BindingService;
import com.beizhi.smartwash.application.service.CabinetService;
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
public class CabinetController {
  private final CabinetService cabinetService;
  private final BindingService bindingService;

  public CabinetController(CabinetService cabinetService, BindingService bindingService) {
    this.cabinetService = cabinetService;
    this.bindingService = bindingService;
  }

  @GetMapping("/assets/cabinets")
  public PageResponse listCabinets(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(cabinetService.listCabinets(page, size), page, size, cabinetService.countCabinets());
  }

  @GetMapping("/assets/cabinets/{cabinetId}")
  public ApiResponse getCabinet(@PathVariable("cabinetId") String cabinetId) {
    return ApiResponse.ok(cabinetService.getCabinet(cabinetId));
  }

  @GetMapping("/assets/cabinets/{cabinetId}/slots")
  public ApiResponse getSlots(@PathVariable("cabinetId") String cabinetId) {
    return ApiResponse.ok(cabinetService.getSlots(cabinetId));
  }

  @PostMapping("/iot/cabinets/{cabinetId}/commands/open-slot")
  public ApiResponse openSlot(
    @PathVariable("cabinetId") String cabinetId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    Map<String, Object> payload = body == null ? Map.of() : body;
    return ApiResponse.ok(cabinetService.openSlot(cabinetId, payload, idempotencyKey));
  }

  @PostMapping("/iot/cabinets/{cabinetId}/commands/ota")
  public ApiResponse issueOtaCommand(
    @PathVariable("cabinetId") String cabinetId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    Map<String, Object> payload = body == null ? Map.of() : body;
    return ApiResponse.ok(cabinetService.issueOtaCommand(cabinetId, payload, idempotencyKey));
  }

  @PostMapping("/iot/cabinets/{cabinetId}/commands/remote")
  public ApiResponse issueRemoteCommand(
    @PathVariable("cabinetId") String cabinetId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    Map<String, Object> payload = body == null ? Map.of() : body;
    return ApiResponse.ok(cabinetService.issueRemoteCommand(cabinetId, payload, idempotencyKey));
  }

  @GetMapping("/iot/cabinets/{cabinetId}/events")
  public ApiResponse listEvents(@PathVariable("cabinetId") String cabinetId) {
    return ApiResponse.ok(cabinetService.listEvents(cabinetId));
  }

  @GetMapping("/asset/cabinets/{cabinetId}/store-bindings")
  public ApiResponse listBindings(@PathVariable("cabinetId") String cabinetId) {
    return ApiResponse.ok(bindingService.listBindings(cabinetId));
  }

  @PostMapping("/asset/cabinets/{cabinetId}/store-bindings")
  public ApiResponse createBinding(
    @PathVariable("cabinetId") String cabinetId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(bindingService.createBinding(cabinetId, body == null ? Map.of() : body, idempotencyKey));
  }

  @PatchMapping("/asset/cabinet-store-bindings/{bindingId}")
  public ApiResponse updateBinding(@PathVariable("bindingId") String bindingId, @RequestBody(required = false) Map<String, Object> body) {
    return ApiResponse.ok(bindingService.updateBinding(bindingId, body == null ? Map.of() : body));
  }

  @PostMapping("/asset/cabinet-store-bindings/{bindingId}/approve")
  public ApiResponse approveBinding(@PathVariable("bindingId") String bindingId) {
    return ApiResponse.ok(bindingService.approveBinding(bindingId));
  }
}
