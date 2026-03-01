package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.AbacService;
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
public class AbacController {
  private final AbacService abacService;

  public AbacController(AbacService abacService) {
    this.abacService = abacService;
  }

  @GetMapping("/abac/policies")
  public PageResponse listPolicies(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(abacService.listPolicies(page, size), page, size, abacService.countPolicies());
  }

  @PostMapping("/abac/policies")
  public ApiResponse createPolicy(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(abacService.createPolicy(body == null ? Map.of() : body, idempotencyKey));
  }

  @PutMapping("/abac/policies/{policyId}")
  public ApiResponse updatePolicy(@PathVariable("policyId") String policyId, @RequestBody(required = false) Map<String, Object> body) {
    return ApiResponse.ok(abacService.updatePolicy(policyId, body == null ? Map.of() : body));
  }

  @GetMapping("/access-grants")
  public PageResponse listAccessGrants(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(abacService.listAccessGrants(page, size), page, size, abacService.countAccessGrants());
  }

  @PostMapping("/access-grants")
  public ApiResponse issueAccessGrant(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(abacService.issueAccessGrant(body == null ? Map.of() : body, idempotencyKey));
  }
}
