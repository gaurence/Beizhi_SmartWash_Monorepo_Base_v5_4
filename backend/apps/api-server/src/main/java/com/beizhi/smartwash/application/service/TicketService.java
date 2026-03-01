package com.beizhi.smartwash.application.service;

import com.beizhi.smartwash.domain.model.TicketRecord;
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
public class TicketService {
  private final InMemoryPlatformRepository repository;
  private final PermissionGuard permissionGuard;
  private final IdempotencyService idempotencyService;
  private final AuditService auditService;

  public TicketService(
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

  public List<TicketRecord> listTickets(int page, int size) {
    permissionGuard.check("finance.ticket.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.listTickets(tenantId, page, size);
  }

  public long countTickets() {
    return repository.countTickets(RequestContextHolder.get().tenantId());
  }

  public TicketRecord getTicket(String ticketId) {
    permissionGuard.check("finance.ticket.read");
    String tenantId = RequestContextHolder.get().tenantId();
    return repository.findTicket(tenantId, ticketId)
      .orElseThrow(() -> new NotFoundException("Ticket not found"));
  }

  public Object createTicket(Map<String, Object> body, String idempotencyKey) {
    permissionGuard.check("finance.ticket.manage");
    RequestContext context = RequestContextHolder.get();

    String orderId = asString(body.get("orderId"));
    String summary = requireText(body.get("summary"), "summary is required");
    String description = asString(body.get("description"));
    String category = normalizeEnumText(body.get("category"), "GENERAL");
    String priority = normalizeEnumText(body.get("priority"), "P2");
    String sourceChannel = normalizeEnumText(body.get("sourceChannel"), "CS_PORTAL");
    int evidenceCount = requireNonNegativeInt(body.get("evidenceCount"));

    String requestHash = String.join(
      "|",
      context.tenantId(),
      orderId,
      summary,
      description,
      category,
      priority,
      sourceChannel,
      String.valueOf(evidenceCount)
    );

    return idempotencyService.execute(context.tenantId(), "finance-ticket-create", idempotencyKey, requestHash, () -> {
      TicketRecord ticket = new TicketRecord(
        UUID.randomUUID().toString(),
        context.tenantId(),
        orderId,
        sourceChannel,
        category,
        priority,
        "OPEN",
        summary,
        description,
        evidenceCount,
        context.userId(),
        "",
        context.requestId()
      );
      repository.saveTicket(ticket);
      auditService.append(
        "RECONCILIATION_TICKET_CREATE",
        "ticket:" + ticket.ticketId(),
        "SUCCESS",
        Map.of(
          "ticketId", ticket.ticketId(),
          "orderId", orderId,
          "category", category,
          "priority", priority,
          "status", ticket.status()
        )
      );
      return ticket;
    });
  }

  private String requireText(Object value, String message) {
    String text = asString(value).trim();
    if (!StringUtils.hasText(text)) {
      throw new BadRequestException(message);
    }
    return text;
  }

  private String normalizeEnumText(Object value, String fallback) {
    String text = asString(value).trim().toUpperCase();
    if (!StringUtils.hasText(text)) {
      return fallback;
    }
    return text;
  }

  private int requireNonNegativeInt(Object value) {
    String text = asString(value).trim();
    if (!StringUtils.hasText(text)) {
      return 0;
    }
    try {
      int parsed = Integer.parseInt(text);
      if (parsed < 0) {
        throw new BadRequestException("evidenceCount must be >= 0");
      }
      return parsed;
    } catch (NumberFormatException ex) {
      throw new BadRequestException("evidenceCount must be an integer");
    }
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
