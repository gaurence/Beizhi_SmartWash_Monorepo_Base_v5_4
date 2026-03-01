# ITER-22 Manual QA Log

## Chunk 1 (2026-02-28) - Reconciliation ticket API baseline

### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/TicketRecord.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/TicketService.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/FinanceController.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`
- `docs/qa/manual/ITER-22.md`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-22` -> PASS
- `make verify-traceability ITER=ITER-22` -> PASS
- `make verify-visual ITER=ITER-22 APPS="customer-app platform-cs"` -> PASS (`mode=mock`)
- `make backend-test` -> PASS
- `make frontend-build APPS="customer-app platform-cs"` -> PASS

### Manual test (role-based)
1. `PLATFORM_CS` (`cs_admin` / `Passw0rd!`): create and query reconciliation ticket
   - Call `POST /finance/reconciliation/tickets` with headers:
     - `Authorization: Bearer mock-jwt:tenant-a:cs_admin`
     - `X-Permissions: finance.ticket.manage,finance.ticket.read`
     - `Idempotency-Key: iter22-ticket-create-001`
   - Body example:
     - `{ "orderId": "ORD-TENANT-A-0001", "summary": "Customer invoice mismatch", "description": "Need manual reconciliation", "category": "RECONCILIATION", "priority": "P2", "sourceChannel": "CS_PORTAL", "evidenceCount": 1 }`
   - Expected:
     - 200 OK, `data.ticketId` returned, `data.tenantId=tenant-a`, `data.status=OPEN`.
   - Repeat same request with same `Idempotency-Key`.
   - Expected:
     - same `ticketId` and same `requestId` (idempotent).
2. `PLATFORM_CS`: tenant isolation on detail read
   - Use created `ticketId`, call `GET /finance/reconciliation/tickets/{ticketId}` with tenant-b token (`Authorization: Bearer mock-jwt:tenant-b:cs_admin`, `X-Permissions: finance.ticket.read`).
   - Expected:
     - 404 Not Found (cross-tenant invisible).
3. `PLATFORM_ADMIN` (`pa_admin` / `Passw0rd!`): audit append-only verification
   - Call `GET /audit/logs` with `Authorization: Bearer mock-jwt:tenant-a:pa_admin` and `X-Permissions: audit.log.read`.
   - Expected:
     - at least one action `RECONCILIATION_TICKET_CREATE`, with `requestId`, actor, tenant, resource, result present.

### Closed TODO (next chunk)
- Closed on 2026-02-28: merged into consolidated completion pass.

### Commit status
- Attempted commit command:
  - `git add backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/TicketRecord.java backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/TicketService.java backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/FinanceController.java backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java docs/qa/manual/ITER-22.md && git commit -m "ITER-22: add reconciliation ticket api baseline"`
- Result in this sandbox:
  - FAIL (`fatal: Unable to create '.git/index.lock': Operation not permitted`)
- Required local follow-up:
  - Run the same `git add` + `git commit` command in an environment where `.git` is writable.
