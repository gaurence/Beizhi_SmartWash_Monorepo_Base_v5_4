package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.RiskService;
import com.beizhi.smartwash.common.ApiResponse;
import com.beizhi.smartwash.common.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RiskController {
  private final RiskService riskService;

  public RiskController(RiskService riskService) {
    this.riskService = riskService;
  }

  @GetMapping("/risk/policies")
  public PageResponse listRiskPolicies(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(
      riskService.listPolicies(page, size),
      page,
      size,
      riskService.countPolicies()
    );
  }

  @PutMapping("/risk/policies/{policyId}")
  public ApiResponse updateRiskPolicy(
    @PathVariable("policyId") String policyId,
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(
      riskService.updatePolicy(policyId, body == null ? Map.of() : body, idempotencyKey)
    );
  }
}
