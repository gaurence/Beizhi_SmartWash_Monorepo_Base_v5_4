package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.CabinetStoreBinding;
import com.beizhi.smartwash.infrastructure.repository.InMemoryPlatformRepository;
import com.beizhi.smartwash.interfaces.rest.error.BadRequestException;
import com.beizhi.smartwash.interfaces.rest.error.NotFoundException;
import com.beizhi.smartwash.security.PermissionGuard;
import com.beizhi.smartwash.security.RequestContext;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BindingService {
  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public BindingService(
    InMemoryPlatformRepository repository,
    PermissionGuard permissionGuard,
    IdempotencyService idempotencyService,
    AuditService auditService
  ) {
    this.repository = repository;
    this.permissionGuard = permissionGuard;
    this.idempotencyService = idempotencyService;
    this.auditService = auditService;
  }

  public List<CabinetStoreBinding> listBindings(String cabinetId) {
    permissionGuard.check("asset.binding.manage");
    RequestContext context = RequestContextHolder.get();
    ensureCabinetExists(context.tenantId(), cabinetId);
    return repository.listBindings(context.tenantId(), cabinetId);
  }

  public Object createBinding(String cabinetId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("asset.binding.manage");
    RequestContext context = RequestContextHolder.get();
    ensureCabinetExists(context.tenantId(), cabinetId);

    String storeId = asString(body.get("storeId"));
    String bindingType = asString(body.get("bindingType"));

    if (!StringUtils.hasText(storeId)) {
      throw new BadRequestException("storeId is required");
    }
    if (!StringUtils.hasText(bindingType)) {
      bindingType = "SHARED";
    }
    final String finalBindingType = bindingType;

    String requestHash = cabinetId + "|" + storeId + "|" + finalBindingType;

    return idempotencyService.execute(context.tenantId(), "binding-create", idempotencyKey, requestHash, () -> {
      CabinetStoreBinding binding = new CabinetStoreBinding(
        UUID.randomUUID().toString(),
        context.tenantId(),
        cabinetId,
        storeId,
        finalBindingType,
        "PENDING"
      );
      repository.saveBinding(binding);

      auditService.append(
        "CABINET_STORE_BINDING_CREATE",
        "binding:" + binding.id(),
        "SUCCESS",
        Map.of("cabinetId", cabinetId, "storeId", storeId, "bindingType", finalBindingType)
      );

      return binding;
    });
  }

  public CabinetStoreBinding updateBinding(String bindingId, Map<String, Object> body) {
    permissionGuard.check("asset.binding.manage");
    RequestContext context = RequestContextHolder.get();

    CabinetStoreBinding binding = repository.findBinding(context.tenantId(), bindingId)
      .orElseThrow(() -> new NotFoundException("Binding not found"));

    binding.update(asString(body.get("storeId")), asString(body.get("bindingType")), asString(body.get("status")));

    auditService.append(
      "CABINET_STORE_BINDING_UPDATE",
      "binding:" + binding.id(),
      "SUCCESS",
      Map.of("status", binding.status(), "bindingType", binding.bindingType(), "storeId", binding.storeId())
    );

    return binding;
  }

  public CabinetStoreBinding approveBinding(String bindingId) {
    permissionGuard.check("asset.binding.manage");
    RequestContext context = RequestContextHolder.get();

    CabinetStoreBinding binding = repository.findBinding(context.tenantId(), bindingId)
      .orElseThrow(() -> new NotFoundException("Binding not found"));

    binding.approve(context.userId());

    auditService.append(
      "CABINET_STORE_BINDING_APPROVE",
      "binding:" + binding.id(),
      "SUCCESS",
      Map.of("approvedBy", context.userId())
    );

    return binding;
  }

  private void ensureCabinetExists(String tenantId, String cabinetId) {
    if (repository.findCabinet(tenantId, cabinetId).isEmpty()) {
      throw new NotFoundException("Cabinet not found");
    }
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
