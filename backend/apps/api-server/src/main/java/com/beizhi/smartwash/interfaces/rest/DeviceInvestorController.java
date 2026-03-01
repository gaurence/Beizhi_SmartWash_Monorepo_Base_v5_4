package com.beizhi.smartwash.interfaces.rest;

import com.beizhi.smartwash.application.service.DeviceInvestorService;
import com.beizhi.smartwash.common.ApiResponse;
import com.beizhi.smartwash.common.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeviceInvestorController {
  private final DeviceInvestorService deviceInvestorService;

  public DeviceInvestorController(DeviceInvestorService deviceInvestorService) {
    this.deviceInvestorService = deviceInvestorService;
  }

  @GetMapping("/asset/device-investors")
  public PageResponse listInvestors(
    @RequestParam(name = "page", defaultValue = "1") int page,
    @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    return PageResponse.of(
      deviceInvestorService.listInvestors(page, size),
      page,
      size,
      deviceInvestorService.countInvestors()
    );
  }

  @GetMapping("/asset/device-investors/{investorId}")
  public ApiResponse getInvestor(@PathVariable("investorId") String investorId) {
    return ApiResponse.ok(deviceInvestorService.getInvestor(investorId));
  }

  @GetMapping("/asset/device-investors/{investorId}/cabinets")
  public ApiResponse listInvestorCabinets(@PathVariable("investorId") String investorId) {
    return ApiResponse.ok(deviceInvestorService.listInvestorCabinets(investorId));
  }
}
