package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.Cabinet;
import com.beizhi.smartwash.domain.model.DeviceInvestor;
import com.beizhi.smartwash.infrastructure.repository.InMemoryPlatformRepository;
import com.beizhi.smartwash.interfaces.rest.error.NotFoundException;
import com.beizhi.smartwash.security.PermissionGuard;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DeviceInvestorService {
  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;

  public DeviceInvestorService(InMemoryPlatformRepository repository, PermissionGuard permissionGuard) {
    this.repository = repository;
    this.permissionGuard = permissionGuard;
  }

  public List<Map<String, Object>> listInvestors(int page, int size) {
    permissionGuard.check("asset.investor.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.listDeviceInvestors(tenantId, page, size).stream()
      .map(this::toView)
      .toList();
  }

  public long countInvestors() {
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.countDeviceInvestors(tenantId);
  }

  public Map<String, Object> getInvestor(String investorId) {
    permissionGuard.check("asset.investor.read");
    String tenantId = RequestContextHolder.get().tenantId();
    DeviceInvestor investor = repository.findDeviceInvestor(tenantId, investorId)
      .orElseThrow(() -> new NotFoundException("Device investor not found"));
    return toView(investor);
  }

  public List<Cabinet> listInvestorCabinets(String investorId) {
    permissionGuard.check("asset.investor.read");
    String tenantId = RequestContextHolder.get().tenantId();
    repository.findDeviceInvestor(tenantId, investorId)
      .orElseThrow(() -> new NotFoundException("Device investor not found"));
    return repository.listInvestorCabinets(tenantId, investorId);
  }

  private Map<String, Object> toView(DeviceInvestor investor) {
    String tenantId = investor.tenantId();
    int cabinetCount = repository.listInvestorCabinets(tenantId, investor.id()).size();
    return Map.of(
      "investorId", investor.id(),
      "name", investor.name(),
      "investorType", investor.investorType(),
      "status", investor.status(),
      "visibleCabinetCount", cabinetCount
    );
  }
}
