# ITER-06 Manual QA Runbook (Incremental)

## Chunk 2026-02-28-01 — Finance payout-account backend APIs

### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/PayoutAccount.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/PayoutAccountService.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/FinanceController.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`
- `docs/qa/manual/ITER-06.md`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-06` -> PASS
- `make verify-traceability ITER=ITER-06` -> PASS
- `make verify-visual ITER=ITER-06` -> PASS (mock mode)
- `make backend-test` -> PASS
- `make frontend-build APPS="platform-admin agent-admin store-admin device-investor"` -> PASS

### How to manually test (role-based)
- `PLATFORM_FINANCE` / `PLATFORM_ADMIN` positive path:
  1. Login with `fin_admin / Passw0rd!` (or `pa_admin / Passw0rd!`).
  2. Call `GET /finance/payout-accounts` with tenant header for target tenant and permission `finance.payout.read`.
  3. Expect paged data and only current tenant accounts.
  4. Call `POST /finance/payout-accounts` with `Idempotency-Key` and payload:
     - `ownerOrgId`, `accountType`, `accountNo`, `holderName`, optional `channelMerchantId`.
  5. Expect account created with masked account number and status `VERIFYING`.
  6. Re-send same request with same `Idempotency-Key`; expect same response object (idempotent).
  7. Call `PATCH /finance/payout-accounts/{accountId}` to update holder/status.
  8. Call `POST /finance/payout-accounts/{accountId}/submit-review` with `Idempotency-Key`; expect status `VERIFYING` and submitter metadata.
  9. Verify audit log includes actions:
     - `PAYOUT_ACCOUNT_CREATE`
     - `PAYOUT_ACCOUNT_UPDATE`
     - `PAYOUT_ACCOUNT_SUBMIT_REVIEW`

- negative path (permission / tenant isolation / validation):
  1. Call payout-account APIs without required permission (`finance.payout.read` or `finance.payout.manage`).
  2. Expect `403 FORBIDDEN`.
  3. Use `tenant-b` headers to operate on `tenant-a` accountId.
  4. Expect `404 NOT_FOUND`.
  5. Submit invalid payload (`accountType=INVALID` or missing `accountNo` on create).
  6. Expect `400 BAD_REQUEST`.

### Closed TODO (next chunk)
- Closed on 2026-02-28: merged into consolidated completion pass.

