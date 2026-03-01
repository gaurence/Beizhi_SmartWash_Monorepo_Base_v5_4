package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.StaffWorkforceService;
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
public class StaffWorkforceController {
  private final StaffWorkforceService staffWorkforceService;

  public StaffWorkforceController(StaffWorkforceService staffWorkforceService) {
    this.staffWorkforceService = staffWorkforceService;
  }

  @GetMapping("/staff")
  public PageResponse listStaff(
    @RequestParam(name = "staffType", required = false) String staffType,
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(
      staffWorkforceService.listStaff(staffType, page, size),
      page,
      size,
      staffWorkforceService.countStaff(staffType)
    );
  }

  @GetMapping("/staff/{staffId}")
  public ApiResponse getStaff(@PathVariable("staffId") String staffId) {
    return ApiResponse.ok(staffWorkforceService.getStaff(staffId));
  }

  @PostMapping("/staff")
  public ApiResponse createStaff(
    @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
    @RequestBody(required = false) Map<String, Object> body
  ) {
    return ApiResponse.ok(staffWorkforceService.createStaff(body == null ? Map.of() : body, idempotencyKey));
  }
}
