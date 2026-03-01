# ITER-17 Manual QA Log

## Chunk 01 - Settlement API baseline
### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/FinanceController.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/SettlementService.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`
- `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`
- `docs/qa/manual/ITER-17.md`

### What was verified (commands + result)
- `make verify-docs` - pass
- `make verify-openapi` - pass
- `make verify-asyncapi` - pass
- `make verify-page-coverage ITER=ITER-17` - pass
- `make verify-traceability ITER=ITER-17` - pass
- `make verify-visual ITER=ITER-17` - pass (`mock` mode in current environment; Playwright disabled)
- `make backend-test` - pass
- `make frontend-build APPS="platform-admin platform-finance store-admin agent-admin device-investor property-portal technician-app"` - pass
- `git add ... && git commit -m "ITER-17: settlement api baseline"` - blocked in current sandbox (`.git/index.lock` write denied: Operation not permitted)

### How to manually test (role-based steps)
- Role: `PLATFORM_FINANCE` (`fin_admin` / `Passw0rd!`)
  1. Call `GET /finance/settlements` with permission `finance.settlement.read`; expect only current tenant settlements.
  2. Use a returned `settlementId` and call `GET /finance/settlements/{settlementId}`; expect 200 with same tenant data.
  3. Call `GET /finance/settlements/{settlementId}/lines`; expect line items and `total > 0`.
  4. Call `POST /finance/settlements/{settlementId}/export` twice with same `Idempotency-Key`; expect same `exportId`, `downloadUrl`, and `requestId`.
- Role: cross-tenant negative case
  1. With tenant-b context, call `GET /finance/settlements/{tenant-a settlementId}` and expect `404`.
- Role: `PLATFORM_ADMIN` or finance auditor with `audit.log.read`
  1. After export, call `GET /audit/logs`.
  2. Verify action `SETTLEMENT_EXPORT_CREATE` exists with resource `settlement:{settlementId}`.

### Closed TODO for next chunk
- Closed on 2026-02-28: merged into consolidated completion pass.

