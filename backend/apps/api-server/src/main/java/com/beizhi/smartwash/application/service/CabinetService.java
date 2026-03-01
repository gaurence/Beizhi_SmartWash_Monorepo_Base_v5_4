package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.Cabinet;
import com.beizhi.smartwash.domain.model.Slot;
import com.beizhi.smartwash.domain.model.SlotState;
import com.beizhi.smartwash.domain.service.SlotStateMachine;
import com.beizhi.smartwash.infrastructure.repository.InMemoryPlatformRepository;
import com.beizhi.smartwash.interfaces.rest.error.BadRequestException;
import com.beizhi.smartwash.interfaces.rest.error.ConflictException;
import com.beizhi.smartwash.interfaces.rest.error.NotFoundException;
import com.beizhi.smartwash.security.PermissionGuard;
import com.beizhi.smartwash.security.RequestContext;
import com.beizhi.smartwash.security.RequestContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CabinetService {
  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final SlotStateMachine slotStateMachine;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public CabinetService(
    InMemoryPlatformRepository repository,
    PermissionGuard permissionGuard,
    SlotStateMachine slotStateMachine,
    IdempotencyService idempotencyService,
    AuditService auditService
  ) {
    this.repository = repository;
    this.permissionGuard = permissionGuard;
    this.slotStateMachine = slotStateMachine;
    this.idempotencyService = idempotencyService;
    this.auditService = auditService;
  }

  public List<Cabinet> listCabinets(int page, int size) {
    permissionGuard.check("asset.cabinet.read");
    RequestContext context = RequestContextHolder.get();
    return repository.listCabinets(context.tenantId(), page, size);
  }

  public long countCabinets() {
    RequestContext context = RequestContextHolder.get();
    return repository.countCabinets(context.tenantId());
  }

  public Cabinet getCabinet(String cabinetId) {
    permissionGuard.check("asset.cabinet.read");
    RequestContext context = RequestContextHolder.get();
    return repository.findCabinet(context.tenantId(), cabinetId)
      .orElseThrow(() -> new NotFoundException("Cabinet not found"));
  }

  public List<Slot> getSlots(String cabinetId) {
    permissionGuard.check("asset.cabinet.read");
    RequestContext context = RequestContextHolder.get();
    ensureCabinetExists(context.tenantId(), cabinetId);
    return repository.listSlots(context.tenantId(), cabinetId);
  }

  public Object openSlot(String cabinetId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("iot.open-slot.execute");

    RequestContext context = RequestContextHolder.get();
    ensureCabinetExists(context.tenantId(), cabinetId);

    String slotNo = asString(body.get("slotNo"));
    if (!StringUtils.hasText(slotNo)) {
      throw new BadRequestException("slotNo is required");
    }

    String nextStateRaw = asString(body.get("nextState"));
    String action = StringUtils.hasText(asString(body.get("action"))) ? asString(body.get("action")) : "OPEN_SLOT";
    String requestHash = cabinetId + "|" + slotNo + "|" + nextStateRaw + "|" + action;

    Object response = idempotencyService.execute(
      context.tenantId(),
      "open-slot",
      idempotencyKey,
      requestHash,
      () -> repository.withSlotLock(context.tenantId(), cabinetId, slotNo, () -> runOpenSlot(context, cabinetId, slotNo, nextStateRaw, action))
    );

    return response;
  }

  public List<Map<String, Object>> listEvents(String cabinetId) {
    permissionGuard.check("asset.cabinet.read");
    RequestContext context = RequestContextHolder.get();
    ensureCabinetExists(context.tenantId(), cabinetId);
    return repository.listEvents(context.tenantId(), cabinetId);
  }

  public Object issueOtaCommand(String cabinetId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("iot.open-slot.execute");

    RequestContext context = RequestContextHolder.get();
    ensureCabinetExists(context.tenantId(), cabinetId);

    String targetVersion = asString(body.get("targetVersion"));
    if (!StringUtils.hasText(targetVersion)) {
      throw new BadRequestException("targetVersion is required");
    }

    String packageUrl = asString(body.get("packageUrl"));
    String requestHash = "ota|" + cabinetId + "|" + targetVersion + "|" + packageUrl;

    return idempotencyService.execute(
      context.tenantId(),
      "iot-ota-command",
      idempotencyKey,
      requestHash,
      () -> runOtaCommand(context, cabinetId, targetVersion, packageUrl)
    );
  }

  public Object issueRemoteCommand(String cabinetId, Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("iot.open-slot.execute");

    RequestContext context = RequestContextHolder.get();
    ensureCabinetExists(context.tenantId(), cabinetId);

    String commandName = asString(body.get("commandName"));
    if (!StringUtils.hasText(commandName)) {
      throw new BadRequestException("commandName is required");
    }

    String argsSignature = String.valueOf(body.getOrDefault("args", Map.of()));
    String requestHash = "remote|" + cabinetId + "|" + commandName + "|" + argsSignature;

    return idempotencyService.execute(
      context.tenantId(),
      "iot-remote-command",
      idempotencyKey,
      requestHash,
      () -> runRemoteCommand(context, cabinetId, commandName, body.get("args"))
    );
  }

  private Object runOpenSlot(RequestContext context, String cabinetId, String slotNo, String nextStateRaw, String action) {
    Slot slot = repository.findSlot(context.tenantId(), cabinetId, slotNo)
      .orElseThrow(() -> new NotFoundException("Slot not found"));

    if (!slotStateMachine.canOpen(slot.state())) {
      throw new ConflictException("Slot is not openable in state: " + slot.state());
    }

    SlotState fromState = slot.state();
    SlotState toState = fromState;

    if (StringUtils.hasText(nextStateRaw)) {
      try {
        toState = SlotState.valueOf(nextStateRaw.trim().toUpperCase());
      } catch (IllegalArgumentException ex) {
        throw new BadRequestException("Invalid nextState: " + nextStateRaw);
      }

      if (toState != fromState) {
        slotStateMachine.assertTransitionAllowed(fromState, toState);
        slot.transitionTo(toState);
      }
    }

    String commandId = UUID.randomUUID().toString();
    repository.appendEvent(context.tenantId(), cabinetId, "OPEN_SLOT", Map.of(
      "slotNo", slotNo,
      "action", action,
      "fromState", fromState.name(),
      "toState", toState.name(),
      "requestId", context.requestId()
    ));

    auditService.append(
      "IOT_OPEN_SLOT",
      "cabinet:" + cabinetId + "/slot:" + slotNo,
      "SUCCESS",
      Map.of("fromState", fromState.name(), "toState", toState.name(), "action", action)
    );

    return Map.of(
      "commandId", commandId,
      "requestId", context.requestId(),
      "cabinetId", cabinetId,
      "slotNo", slotNo,
      "fromState", fromState.name(),
      "toState", toState.name(),
      "issuedAt", OffsetDateTime.now().toString()
    );
  }

  private Object runOtaCommand(RequestContext context, String cabinetId, String targetVersion, String packageUrl) {
    String commandId = UUID.randomUUID().toString();

    Map<String, Object> eventPayload = new LinkedHashMap<>();
    eventPayload.put("requestId", context.requestId());
    eventPayload.put("targetVersion", targetVersion);
    if (StringUtils.hasText(packageUrl)) {
      eventPayload.put("packageUrl", packageUrl);
    }
    repository.appendEvent(context.tenantId(), cabinetId, "OTA_COMMAND", eventPayload);

    Map<String, Object> auditDetails = new LinkedHashMap<>();
    auditDetails.put("targetVersion", targetVersion);
    if (StringUtils.hasText(packageUrl)) {
      auditDetails.put("packageUrl", packageUrl);
    }
    auditService.append(
      "IOT_OTA_COMMAND",
      "cabinet:" + cabinetId,
      "SUCCESS",
      auditDetails
    );

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("commandId", commandId);
    response.put("requestId", context.requestId());
    response.put("cabinetId", cabinetId);
    response.put("commandType", "OTA");
    response.put("targetVersion", targetVersion);
    if (StringUtils.hasText(packageUrl)) {
      response.put("packageUrl", packageUrl);
    }
    response.put("status", "ISSUED");
    response.put("issuedAt", OffsetDateTime.now().toString());
    return response;
  }

  private Object runRemoteCommand(RequestContext context, String cabinetId, String commandName, Object args) {
    String commandId = UUID.randomUUID().toString();

    Map<String, Object> eventPayload = new LinkedHashMap<>();
    eventPayload.put("requestId", context.requestId());
    eventPayload.put("commandName", commandName);
    if (args != null) {
      eventPayload.put("args", args);
    }
    repository.appendEvent(context.tenantId(), cabinetId, "REMOTE_COMMAND", eventPayload);

    Map<String, Object> auditDetails = new LinkedHashMap<>();
    auditDetails.put("commandName", commandName);
    if (args != null) {
      auditDetails.put("args", args);
    }
    auditService.append(
      "IOT_REMOTE_COMMAND",
      "cabinet:" + cabinetId,
      "SUCCESS",
      auditDetails
    );

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("commandId", commandId);
    response.put("requestId", context.requestId());
    response.put("cabinetId", cabinetId);
    response.put("commandType", "REMOTE");
    response.put("commandName", commandName);
    if (args != null) {
      response.put("args", args);
    }
    response.put("status", "ISSUED");
    response.put("issuedAt", OffsetDateTime.now().toString());
    return response;
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
