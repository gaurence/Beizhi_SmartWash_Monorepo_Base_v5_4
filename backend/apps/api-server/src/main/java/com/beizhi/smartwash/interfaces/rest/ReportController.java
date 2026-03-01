package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.ReportService;
import com.beizhi.smartwash.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ReportController {
  private final ReportService reportService;

  public ReportController(ReportService reportService) {
    this.reportService = reportService;
  }

  @GetMapping("/reports/summary")
  public ApiResponse getSummary(@RequestParam Map<String, Object> query) {
    return ApiResponse.ok(reportService.getSummary(query == null ? Map.of() : query));
  }

  @PostMapping("/reports/export")
  public ApiResponse exportReport(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(reportService.exportReport(body == null ? Map.of() : body, idempotencyKey));
  }
}
