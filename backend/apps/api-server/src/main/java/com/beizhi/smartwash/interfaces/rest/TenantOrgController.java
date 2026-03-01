package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.TenantOrgService;
import com.beizhi.smartwash.common.ApiResponse;
import com.beizhi.smartwash.common.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TenantOrgController {
  private final TenantOrgService tenantOrgService;

  public TenantOrgController(TenantOrgService tenantOrgService) {
    this.tenantOrgService = tenantOrgService;
  }

  @GetMapping("/tenants")
  public PageResponse listTenants(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(tenantOrgService.listTenants(page, size), page, size, tenantOrgService.countTenants());
  }

  @PostMapping("/tenants")
  public ApiResponse createTenant(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(tenantOrgService.createTenant(body == null ? Map.of() : body, idempotencyKey));
  }

  @GetMapping("/tenants/{tenantId}")
  public ApiResponse getTenant(@PathVariable("tenantId") String tenantId) {
    return ApiResponse.ok(tenantOrgService.getTenant(tenantId));
  }

  @PutMapping("/tenants/{tenantId}")
  public ApiResponse updateTenant(@PathVariable("tenantId") String tenantId, @RequestBody(required = false) Map<String, Object> body) {
    return ApiResponse.ok(tenantOrgService.updateTenant(tenantId, body == null ? Map.of() : body));
  }

  @GetMapping("/tenants/{tenantId}/orgs")
  public ApiResponse listOrgs(@PathVariable("tenantId") String tenantId) {
    return ApiResponse.ok(tenantOrgService.listTenantOrgs(tenantId));
  }

  @GetMapping("/tenants/{tenantId}/stores")
  public ApiResponse listStores(@PathVariable("tenantId") String tenantId) {
    return ApiResponse.ok(tenantOrgService.listTenantStores(tenantId));
  }

  @PostMapping("/tenants/{tenantId}/stores")
  public ApiResponse createStore(
    @PathVariable("tenantId") String tenantId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(tenantOrgService.createStore(tenantId, body == null ? Map.of() : body, idempotencyKey));
  }

  @PostMapping("/tenants/{tenantId}/orgs")
  public ApiResponse createOrg(
    @PathVariable("tenantId") String tenantId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(tenantOrgService.createOrg(tenantId, body == null ? Map.of() : body, idempotencyKey));
  }

  @GetMapping("/orgs/tree")
  public ApiResponse orgTree() {
    return ApiResponse.ok(tenantOrgService.orgTree());
  }

  @GetMapping("/stores/{storeId}")
  public ApiResponse getStore(@PathVariable("storeId") String storeId) {
    return ApiResponse.ok(tenantOrgService.getStore(storeId));
  }

  @PutMapping("/stores/{storeId}")
  public ApiResponse updateStore(
    @PathVariable("storeId") String storeId,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(tenantOrgService.updateStore(storeId, body == null ? Map.of() : body));
  }

  @GetMapping("/stores/{storeId}/agreements")
  public ApiResponse listStoreAgreements(@PathVariable("storeId") String storeId) {
    return ApiResponse.ok(tenantOrgService.listStoreAgreements(storeId));
  }

  @PostMapping("/stores/{storeId}/agreements")
  public ApiResponse createStoreAgreement(
    @PathVariable("storeId") String storeId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(
      tenantOrgService.createStoreAgreement(storeId, body == null ? Map.of() : body, idempotencyKey)
    );
  }
}
